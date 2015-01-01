(ns leiningen.sablecc.usage 
  (require [leiningen.core.eval :as lein-eval]))

(defn usage
  "SableCC Usage"
  [project]
    (lein-eval/eval-in-project project 
                     `(org.sablecc.sablecc.SableCC/main (clojure.core/into-array java.lang.String []))))