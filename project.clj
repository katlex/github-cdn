(defproject katlex/github-cdn "0.1.3"
  :description "A leiningen plugin to handle release files to Github (Akamai) CDN via github pages"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :github-cdn {:dir "release"
               :repository "git@github.com:katlex/github-cdn"
               :target "test"
               :branch "gh-pages"}
  :eval-in-leiningen true
  :dependencies [[me.raynes/conch "0.8.0"]
                 [me.raynes/fs "1.4.6"]])
