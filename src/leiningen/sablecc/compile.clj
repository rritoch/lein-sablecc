(ns leiningen.sablecc.compile
  (:require [clojure.java.io :as io]
            [robert.hooke]
            [leiningen.compile :as lein-compile]
            [leiningen.javac :as lein-javac])
  (:refer-clojure :exclude [compile])
  (:import [org.sablecc.sablecc SableCC]))


(defn sablecc-source-paths
  [project]
  (if-let [paths (:sablecc-source-paths project)]
      (vec paths)
      []))

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

(def javac-hook-run (atom false))
(def javac-hook-activated (atom false))

(defn javac-hook
  [f project & args]
    (if @javac-hook-run
        (apply f (into [project] args))
        (do (reset! javac-hook-run true)
          (let [target-path (destination-path project)
                target (io/file target-path)]
             (.mkdirs target)
             (.mkdir target)
             (doseq [s (sources project)]
              (SableCC/processGrammar s target))
              (if args
                  (apply f (into [(assoc project :java-source-paths [target-path])] args))
                  (apply f [(assoc project :java-source-paths [target-path])]))
             (apply f (into [project] args))))))

(defn activate
  "Activate SableCC javac hook"
  [& args]
    (or @javac-hook-activated
        (do (reset! javac-hook-activated true)
            (robert.hooke/add-hook #'leiningen.javac/javac #'javac-hook)
            true)))

(defn compile
  "Compile SableCC sources"
  [project]
    (activate project)
    (lein-javac/javac project) ; Runs javac-hook
    (lein-compile/compile project))
  

