[![Clojars Project](https://img.shields.io/clojars/v/katlex/github-cdn.svg)](https://clojars.org/katlex/github-cdn)

# github-cdn

A leiningen plugin to publish files to github pages service (aka github CDN)

## Usage

1. Install [leiningen](http://leiningen.org/).

2. Add a `project.clj` file to your project root.

```
(defproject my/project "0.0.0"
  :github-cdn {;; source directory which content is published
               :dir "release" 
               ;; optional repository to publish (picked up from the project git if not specified)
               :repository "git@github.com:katlex/github-cdn" 
               ;; branch name to push files too (optional gh-pages is default)
               :branch "gh-pages" 
               ;; optional subfolder to publish files to (default is branch root)
	       :target "subfolder"}
```

3. Run `lein github-cdn` in your project root

## Release notes

*v0.1.2*  
Fixed bug with pulling of unlinked branch

*v0.1.1*  
Added feature to push to a subdirectory and better error meesages if something goes wrong

*v0.1.0*  
First release

## Acknowledgements

Made on top and under inspiration of [Dox](https://github.com/Raynes/dox)

Copyright © 2015-2016 Alexey Lunacharsky

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
