(ns leiningen.sablecc.license
  (import [org.sablecc.sablecc SableCC]))

(defn license
  "Display license"
  [project]
     (SableCC/main (into-array String ["--license"])))
  
  