;leiningen.sablecc
; <br />Author: Ralph Ritoch <rritoch@gmail.com>

(ns ^{:author "Ralph Ritoch <rritoch@gmail.com>"
      :doc "SableCC Leiningen Plugin"
    } leiningen.sablecc
  (:use [leiningen.help :only (help-for subtask-help-for)]
        [leiningen.sablecc.compile :only (compile)]
        [leiningen.sablecc.license :only (license)]
        [leiningen.sablecc.usage :only (usage)]))

(defn sablecc
  "Lein-SableCC tasks."
  {:help-arglists '([compile license usage])
   :subtasks [#'compile #'license #'usage]}
  
  ([project]
     (sablecc project nil))
  ([project subtask & args]
     (case subtask
        "compile" (apply compile project args)
        "license" (apply license project args)
        "usage" (apply usage project args)
        (apply usage project args))))

;; End of namespace leiningen.sablecc