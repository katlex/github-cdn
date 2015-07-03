(defproject katlex/github-cdn "0.1.0"
  :description "A leiningen plugin to handle release files to Github (Akamai) CDN via github pages"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :github-cdn {:dir "release"
               :repository "git@github.com:katlex/test"
               :branch "gh-pages"}
  :eval-in-leiningen true
  :dependencies [[me.raynes/conch "0.7.0"]
                 [me.raynes/fs "1.4.5"]])
