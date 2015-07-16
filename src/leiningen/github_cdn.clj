(ns leiningen.github-cdn 
  (:require [me.raynes.conch :refer [programs]]
            [me.raynes.fs :as fs]
            [clojure.string :as s]
            [clojure.java.shell :refer [with-sh-dir]]))

(programs git mv rm ls cp cd)

(def ^:dynamic *dir* fs/*cwd*)

(defn current-branch []
  (s/trim-newline (git "rev-parse" "--abbrev-ref" "HEAD")))

(defn ensure-branch [branch]
  "Checks that current dir   "
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
  [src-dir dest-dir]
  (println "Copying files " (.getAbsolutePath src-dir) (.getAbsolutePath dest-dir))
  (doseq [[dir _ files] (fs/iterate-dir src-dir)
          :let [local-dir (-> dir .getAbsolutePath
                              (s/replace (.getAbsolutePath src-dir) ""))]
          file files
          :let [src-file (fs/file (str src-dir local-dir "/" file))
                dest-file (fs/file (str dest-dir local-dir "/" file))]]
    (println (.getAbsolutePath src-file) (.getAbsolutePath dest-file))
    (fs/copy+ src-file dest-file)))


(defn publish-dir
  [& {:keys [src-dir repository-uri branch comment']
      :or {branch "gh-pages" comment' "Updated files on CDN"}}]
  (let [repository-dir (s/replace (str repository-uri "-" branch) #"[@\.:\/\\]" "-")
        dest-dir (fs/file (fs/home) ".github-cdn" repository-dir)]
    (if src-dir
      (binding [*dir* dest-dir]
        (println "Checking git in" (.getAbsolutePath *dir*))
        (when-not (zero? (try @(:exit-code (git "status" {:dir *dir* :throw false :verbose true}))
                           (catch Throwable _ 1)))
          (println "No git found. Clonning repo" repository-uri)
          (git "clone" repository-uri dest-dir))
        (println "Ensuring branch" branch)
        (ensure-branch branch)
        (println "Removing old files")
        (git "rm" "-rf" "*" {:dir *dir* :throw false})
        (copy-files src-dir dest-dir)
        (println "Commiting and pushing")
        (git "add" "*" {:dir *dir* :throw false})
        (git "commit" "-m" comment' {:dir *dir* :throw false})
        (git "push" "origin" (str branch ":" branch) {:dir *dir*})
        (println "Done"))
      (println "No src dir specified"))))

(defn github-cdn
  "Publishes resources to gihhub branch"
  [project & args]
  (let [settings (:github-cdn project)]
    (publish-dir :src-dir (fs/file (:dir settings))
                 :repository-uri (or (:repository settings) (default-uri))
                 :branch (or (:branch settings) "gh-pages")
                 :comment' (if args (apply str (interpose " " args)) "Updated files on CDN"))))
