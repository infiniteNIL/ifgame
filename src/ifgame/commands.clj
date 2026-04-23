(ns ifgame.commands
  (:require [ifgame.game :as game]
            [ifgame.object :as object]
            [ifgame.room :as room]))

(defn- drop-object [ast game]
  (let [noun (get-in ast [:direct-object :noun])
        obj-key (object/get-key noun)
        error (get-in ast [:direct-object :error])]
    ;(println "noun:" noun)
    (cond
      (= error :no-known-noun)                    (let [noun (first (get-in ast [:direct-object :words]))]
                                                    (println "You're not carrying a" (str noun ".")))

      (or (= noun "all") (= noun "everything"))   (let [object-keys (:inventory @game)
                                                        location (:location @game)]
                                                    (if object-keys
                                                      (doseq [key object-keys]
                                                        (let [obj (object/get-object key)
                                                              name (first (:names obj))]
                                                          (game/remove-from-inventory game key)
                                                          (room/add-object location key)
                                                          (println (str name ":") "Dropped.")))
                                                      (println "You're not carrying anything.")))

      (not (game/in-inventory? game obj-key))     (println "You're not carrying the" (str noun "."))

      :else                                       (let [location (:location @game)]
                                                    (game/remove-from-inventory game obj-key)
                                                    (room/add-object location obj-key)
                                                    (println "Dropped.")))))

(defn- examine [ast]
  (let [noun (get-in ast [:direct-object :noun])
        obj-key (object/get-key noun)
        object (object/get-object obj-key)
        error (get-in ast [:direct-object :error])]
    (cond
      (= error :no-known-noun)    (let [noun (first (get-in ast [:direct-object :words]))]
                                    (println "You see nothing special about the" (str noun ".")))

      (or (= noun "all")
          (= noun "everything"))  (println "You can only examine things one at a time.")

      (:full-description object)  (println (:full-description object))

      :else                       (println "You see nothing special about the" (str noun ".")))))

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
  ;; TODO: Make verbs definable like rooms and objects instead of hard-coded
  (let [verb (:verb ast)
        ;; TODO: need transform, so s -> go south, for example
        ;; TODO: Should just pass direct object and indirect object to action
        ;; TODO: Parser should resolve action, direct object and indirect object
        action (:action ast)
        action-fn (:fn action)]
    (println "process-cmd ast:" ast)
    (when (:error ast)
      (println "error:" (:error ast)))
    (when (:noun ast)
      (println "noun:" (:noun ast)))
    ;(println "action:" (:action ast))
    ;(println "action-fn:" action-fn)
    (when (:direct-object ast)
      (println "do:" (:direct-object ast)))
    (when (:indirect-object ast)
      (println "io:" (:indirect-object ast)))
    (cond
      (= (:error ast) :unknown-action)    (handle-unknown-action (:verb ast))

      (= (:error ast) :no-known-noun)     (handle-unknown-noun (:noun ast))

      action-fn                           (action-fn ast game)

      (= verb "drop")            (drop-object ast game)

      (= verb "examine")         (examine ast)

      :else                      (println "Sorry, I don't know what" (str \" verb \") "means."))))
