(ns leiningen.github-cdn
  (:require [me.raynes.conch :refer [programs]]
            [me.raynes.fs :as fs]
            [clojure.string :as s]
            [clojure.java.shell :refer [with-sh-dir]])
  (:import [clojure.lang ExceptionInfo]))

(programs git)

(def ^:dynamic *dir* fs/*cwd*)

(defn current-branch []
  (s/trim-newline (git "rev-parse" "--abbrev-ref" "HEAD")))

(defn ensure-branch [branch]
  "Checks that current dir   "
  (println "Ensuring branch" branch)
  (if-not (zero? @(:exit-code
                   (git "checkout" branch {:dir *dir* :verbose true :throw false})))
    (do (println (str "*** " branch " branch not found -- creating it ***"))
        (git "rm" "*" {:dir *dir* :throw false})
        (git "checkout" "--orphan" branch {:dir *dir*}))
    (println "Already there")))

(defn default-uri
  "Calculates default uri basing on current repository"
  []
  (first
   (for [line (s/split-lines
               (git "remote" "-v"))
         :let [[remote uri] (s/split line #"\s")]
         :when (= remote "origin")]
     uri)))

(defn copy-files
  "Copies files recursevely from src-dir to dest-dir"
  [src-dir dest-dir]
  (println "Copying files " (.getAbsolutePath src-dir) " --> " (.getAbsolutePath dest-dir))
  (doseq [[dir _ files] (fs/iterate-dir src-dir)
          :let [local-dir (-> dir .getAbsolutePath
                              (s/replace (.getAbsolutePath src-dir) ""))]
          file files
          :let [src-file (fs/file src-dir local-dir file)
                dest-file (fs/file dest-dir local-dir file)]]
    (println (.getAbsolutePath src-file) (.getAbsolutePath dest-file))
    (fs/copy+ src-file dest-file)))

(defn publish-dir
  [& {:keys [src-dir target-dir repository-uri branch comment']}]
  (let [repository-dir (s/replace (str repository-uri "-" branch) #"[@\.:\/\\]" "-")
        dest-dir (fs/file (fs/home) ".github-cdn" repository-dir)]
    (if src-dir
      (binding [*dir* dest-dir]
        (println "Checking git repo in" (.getAbsolutePath *dir*))
        (.mkdirs *dir*)
        (when-not (zero? @(:exit-code (git "status" {:dir *dir* :throw false :verbose true})))
          (println "No git repo found. Clonning repo" repository-uri)
          (git "clone" repository-uri dest-dir))
        (ensure-branch branch)
        (binding [*dir* (fs/file dest-dir target-dir)]
          (.mkdirs *dir*)
          (git "pull" "origin" branch {:dir *dir* :throw false})
          (println "Removing old files")
          (git "rm" "-rf" "*" {:dir *dir* :throw false})
          (copy-files src-dir *dir*)
          (println "Commiting and pushing")
          (git "add" "*" {:dir *dir* :throw false})
          (git "commit" "-m" comment' {:dir *dir* :throw false})
          (git "push" "origin" (str branch ":" branch) {:dir *dir*})
          (println "Done")))
      (println "No source dir specified"))))

(defn github-cdn
  "Publishes resources to gihhub branch"
  [project & args]
  (let [settings (:github-cdn project)]
    (try
      (publish-dir :src-dir (fs/file (:dir settings))
                   :target-dir (or (:target settings) "")
                   :repository-uri (or (:repository settings) (default-uri))
                   :branch (or (:branch settings) "gh-pages")
                   :comment' (if args (apply str (interpose " " args)) "Updated files on CDN"))
      (catch ExceptionInfo e
        (let [error (-> e .getData :proc :err)
              error (or error "Unknown error")
              error (apply str error)]
          (println error))))))
