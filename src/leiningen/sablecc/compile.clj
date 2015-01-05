(ns leiningen.sablecc.compile
  (:require [clojure.java.io :as io]
            [robert.hooke]
            [leiningen.compile :as lein-compile]
            [leiningen.javac :as lein-javac]
            [clojure.string :as string]
            [leiningen.core.eval :refer [eval-in-project]])
  (:refer-clojure :exclude [compile]))

(def javac-hook-run (atom false))

(def javac-hook-activated (atom false))

(defn sablecc-source-paths
  [project]
    (if-let [paths (:sablecc-source-paths project)]
      (vec paths)
      []))

(defn sources
  [project]
    (filter #(.endsWith (.toString %) ".scc")
            (reduce into 
                    #{} 
                    (map (comp file-seq io/file)
                         (sablecc-source-paths project)))))

(defn clean-path 
  [fname]
    (let [f (io/file fname)]
         (when (.isDirectory f)
            (doseq [f2 (.listFiles f)]
              (clean-path f2)))
         (if (.exists f) (io/delete-file f))))

(defn strip-sablecc-comments
  [s]
    (let [len (count s)]
      (loop [idx 0 e false q nil cmt 0 o ""]
        (if (>= idx len)
            o
            (recur (inc idx)
                   #_(and (not e)
                         (= cmt 0)
                         (= \\ (nth s idx)))
                   false
                   (cond (or e (> cmt 0))
                         q
                         (and q
                              (= (nth s idx) q))
                         nil
                         (= (nth s idx) \')
                         \'
                         (= (nth s idx) \")
                         \"
                         :else
                         nil)
                   (cond (or e q)
                         cmt
                         (and (= cmt 0)
                              (< (inc idx) len)
                              (= (nth s idx) \/)
                              (= (nth s (inc idx)) \*))
                         1
                         (= cmt 1)
                         2
                         (and (= cmt 2)
                              (< (inc idx) len)
                              (= (nth s idx) \*)
                              (= (nth s (inc idx)) \/))
                         3
                         (= cmt 3)
                         0
                         (and (= cmt 0)
                              (< (inc idx) len)
                              (= (nth s idx) \/)
                              (= (nth s (inc idx)) \/))
                         8
                         (and (= cmt 8)
                              (or (= (nth s idx) \return)
                                  (= (nth s idx) \newline)))
                         0
                         :else
                         cmt)
                   (cond (= (nth s idx) \return)
                         (str o \return)
                         (= (nth s idx) \newline)
                         (str o \newline)
                         (or (> cmt 0)
                             (and (= cmt 0)
                                  (not e)
                                  (not q)
                                  (< (inc idx) len)
                                  (= (nth s idx) \/)
                                  (= (nth s (inc idx)) \*))
                             (and (= cmt 0)
                                  (not e)
                                  (not q)
                              (< (inc idx) len)
                              (= (nth s idx) \/)
                              (= (nth s (inc idx)) \/)))
                         o
                         :else
                         (str o (nth s idx))))))))

(defn get-sablecc-package
  [f]
    (when-let [r (re-find #"(s?)(^|;)(\s*)\QPackage\E(\s+)([^;\s]*)"  
                          (strip-sablecc-comments (slurp f)))]
      (nth r 5)))

(defn destination-path
  [project]
    (str (:target-path project) 
         "/generated-sources/sablecc/"))

(defn need-compile?
  [project src]
    (when-let [pkg (get-sablecc-package src)]
      (let [pp (io/file (str (destination-path project)
                             "/"
                             (string/replace pkg "." "/")
                             "/parser/Parser.java"))]
           (or (not (.isFile pp)) 
               (> (.lastModified src) (.lastModified pp))))))

(defn clean-gen-sources
  [project src]
    (when-let [pkg (get-sablecc-package src)]
      (let [pp (io/file (str (destination-path project)
                             "/"
                             (string/replace pkg "." "/")))]
           (clean-path pp))))

(defn gen-sources
  "Generate sources returning list of modified packages"
  [project]
   (let [target-path (destination-path project)
         target (io/file target-path)
         cc (fn [s]
                (let [ss (.toString s)]
                    (when (need-compile? project s)
                        (eval-in-project project `(org.sablecc.sablecc.SableCC/processGrammar (clojure.java.io/file ~ss) (clojure.java.io/file ~target-path))
                                       '(require 'clojure.java.io))
                        
                        (get-sablecc-package s))))]
        (doseq [s (sources project)]
                  (if (need-compile? project s)
                      (clean-gen-sources project s)))
        (.mkdirs target)
        (.mkdir target)
        (doall (keep cc (sources project)))))

(defn cp
  [src dest]
     (let [fin (io/file src)
           fout (io/file dest)]
       (.mkdirs (.getParentFile fout))
       (io/copy fin fout)))

(defn after-compile-gen-sources
  [project pkg]
    (let [pkg-path (string/replace pkg "." "/")
          gs-path (str (destination-path project)
                       "/"
                        pkg-path)
          t-path (str (:target-path project)
                      "/classes/"
                      pkg-path)
          rlist ["/parser/parser.dat"
                 "/lexer/lexer.dat"]]
         (doseq [f rlist]
           (cp (str gs-path f)
               (str t-path f)))))

(defn javac-hook
  [f project & args]
    (if @javac-hook-run
        (apply f (into [project] args))
        (do (reset! javac-hook-run true)
          (let [target-path (destination-path project)
                modified-packages (gen-sources project)]
             (if args
                 (apply f (into [(assoc project :java-source-paths [target-path])] args))
                 (apply f [(assoc project :java-source-paths [target-path])]))
             (doseq [pkg modified-packages]
                (after-compile-gen-sources project pkg) 
             (apply f (into [project] args)))))))

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
    (let [modified-packages (gen-sources project)]
       (lein-javac/javac (assoc project :java-source-paths [(destination-path project)]))
       (doseq [pkg modified-packages]
           (after-compile-gen-sources project pkg)))
    (lein-javac/javac project)
    (lein-compile/compile project))
