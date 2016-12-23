(defproject clj-ogl "0.1.0"
  :description "Modern OpenGL bindings for Clojure."
  :url ""
  :main clj-ogl.core
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :jvm-opts ["-Djava.library.path=/Local/Users/john/Documents/clojure/clj-ogl/lib"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.jogamp.gluegen/gluegen "2.3.2"]
                 [org.jogamp.gluegen/gluegen-rt "2.3.2" :classifier "natives-macosx-universal"]
                 [org.jogamp.jogl/jogl-all "2.3.2"]])
