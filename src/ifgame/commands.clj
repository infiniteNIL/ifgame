(ns ifgame.commands
  (:require [clojure.string :as str]
            [ifgame.room :as room]))

(defn- handle-unknown-action [verb]
  (if (nil? verb)
    (println "Excuse me?")
    (println "Sorry, I don't know what" (str \" verb \") "means."))
  true)

(defn- handle-unknown-noun [noun game]
  (let [room-key (:location @game)
        room (room/get-room room-key)
        desc (:desc room)]
    (if (str/includes? desc noun)
      (println "You cannot do anything with the" (str noun "."))
      (println "I don't see any" noun "here."))
    true))

(defn- handle-errors [ast game]
  ;(println "handle-errors")
  (cond
    (= (:error ast) :unknown-action) (handle-unknown-action (:verb ast))
    (= (:error ast) :no-known-noun) (handle-unknown-noun (:noun ast) game)
    :else false))

(defn handle-object-action [key ast game]
  (let [obj (key ast)
        fn (:fn obj)]
    (if fn
      (do
        ;(println "handle" key "action.")
        (fn ast game))
      false)))

(defn- handle-object-actions [ast game]
  (if (handle-object-action :indirect-object ast game)
    true
    (handle-object-action :direct-object ast game)))

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
    ;; BUG: When multiple objects in play (like "get all"), every object involved needs to be given a chance
    ;;      to handle the action
    (cond
      (handle-errors ast game)                          true
      ;(= (:error ast) :unknown-action)    (handle-unknown-action (:verb ast))
      ;(= (:error ast) :no-known-noun)     (handle-unknown-noun (:noun ast))
      (handle-object-action :indirect-object ast game)  true
      (handle-object-action :direct-object ast game)    true
      action-fn                                         (action-fn ast game)
      :else                                             false)))
