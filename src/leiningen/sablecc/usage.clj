(ns leiningen.sablecc.usage
  (import [org.sablecc.sablecc SableCC]))

(defn usage
  "SableCC Usage"
  [project]
    (SableCC/main (into-array String [])))