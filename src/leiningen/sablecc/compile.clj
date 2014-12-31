(ns leiningen.sablecc.compile
  (:require [clojure.java.io :as io]
            [robert.hooke]
            [leiningen.compile :as lein-compile]
            [leiningen.javac :as lein-javac]
            [clojure.string :as string])
  (:refer-clojure :exclude [compile])
  (:import [org.sablecc.sablecc SableCC]))

(def javac-hook-run (atom false))
(def javac-hook-activated (atom false))

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
                            

(defn get-sablecc-package
  [f]
    (when-let [r (re-find #"(s?)(^|\n)\QPackage\E(\s+)([^;\s]*)"  
                          (string/replace (slurp f) 
                                          #"(s?)/\*(.|\r\n)*?\*/" ""))]
      (nth r 4)))
           

(defn destination-path
  [project]
    (str (:target-path project) "/generated-sources/sablecc/"))

(defn need-compile?
  [project src]
    (when-let [pkg (get-sablecc-package src)]
      (let [pp (io/file (str (destination-path project)
                             "/"
                             (string/replace pkg "." "/")
                             "/parser/Parser.java"))]
           (or (not (.isFile pp)) 
               (> (.lastModified src) (.lastModified pp))))))

(defn gen-sources
  [project]
   (let [target-path (destination-path project)
         target (io/file target-path)]
        (.mkdirs target)
        (.mkdir target)
        (doseq [s (sources project)]
               (if (need-compile? project s)
                   (SableCC/processGrammar s target)))))

(defn javac-hook
  [f project & args]
    (if @javac-hook-run
        (apply f (into [project] args))
        (do (reset! javac-hook-run true)
          (let [target-path (destination-path project)]
             (gen-sources project)
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
    (reset! javac-hook-run true) ; Stop hook
    (gen-sources project)
    (lein-javac/javac (assoc project :java-source-paths [(destination-path project)]))
    (lein-javac/javac project)
    (lein-compile/compile project))
  

