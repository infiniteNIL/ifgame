(ns ifgame.commands)

(defn- handle-unknown-action [verb]
  (if (nil? verb)
    (println "Excuse me?")
    (println "Sorry, I don't know what" (str \" verb \") "means."))
  true)

(defn- handle-unknown-noun [noun]
  ;; TODO: We could search room description and if there say something about it's not important.
  (println "I don't see any" noun "here.")
  true)

(defn process-cmd [ast game]
  (let [action (:action ast)
        action-fn (:fn action)]
    ;(println "process-cmd ast:" ast)
    ;(when (:error ast)
    ;  (println "error:" (:error ast)))
    ;(when (:noun ast)
    ;  (println "noun:" (:noun ast)))
    ;(println "action:" (:action ast))
    ;(println "action-fn:" action-fn)
    ;(when (:direct-object ast)
    ;  (println "do:" (:direct-object ast)))
    ;(when (:indirect-object ast)
    ;  (println "io:" (:indirect-object ast)))
    ;; TODO: Give direct and indirect objects a chance to handle action
    (cond
      (= (:error ast) :unknown-action)    (handle-unknown-action (:verb ast))

      (= (:error ast) :no-known-noun)     (handle-unknown-noun (:noun ast))

      action-fn                           (action-fn ast game)

      :else                               false)))
