(ns leiningen.sablecc.license
  (require [leiningen.core.eval :refer [eval-in-project]]))

(defn license
  "Display license"
  [project]
     (eval-in-project project `(org.sablecc.sablecc.SableCC/main (into-array String ["--license"]))))
  
  