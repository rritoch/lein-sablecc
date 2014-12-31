(ns leiningen.sablecc.compile
  (:require [clojure.java.io :as io]
            [leiningen.compile :as lein-compile])
  (:refer-clojure :exclude [compile])
  (:import [org.sablecc.sablecc SableCC]))


(defn sablecc-source-paths
  [project]
  (let [paths (:sablecc-source-paths project)]
       (if paths
           (vec paths)
           [])))

(defn sources
  [project]
    (let [paths (sablecc-source-paths project)]
      (filter #(.endsWith (.toString %) ".scc")
              (reduce into 
                #{} 
                (map (comp file-seq io/file)
                     paths)))))
                            

(defn destination-path
  [project]
    (str (:target-path project) "/generated-sources/sablecc/"))

(defn compile
  "Compile SableCC sources"
  [project]
    (let [target-path (destination-path project)
          target (io/file target-path)]
      (.mkdirs target)
      (.mkdir target)
      (doseq [s (sources project)]
        (SableCC/processGrammar s target))
      (lein-compile/compile (assoc project :java-source-paths [target-path]))
      (lein-compile/compile project)))
  