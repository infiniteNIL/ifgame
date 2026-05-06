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
  (cond
    (= (:error ast) :unknown-action) (handle-unknown-action (:verb ast))
    (= (:error ast) :no-known-noun) (handle-unknown-noun (:noun ast) game)
    :else false))

(defn handle-object-action [key ast game]
  (let [obj (key ast)
        handler (:handler obj)]
    (if handler
      (handler ast game)
      false)))

;; Processing order of objects functions:
;; 1) The indirect object if any
;; 2) The direct object if any
;; 3) The verb
;; 4) The room the player is in
;; TODO: 5) Daemons that have no relation to the player's action
;;
;; If one handles it, the process of command is finished. A function may do something
;; but not handle the command. However, the room handler is always given a chance to handle the command
;; regardless if another handler handled it.
(defn process-cmd [ast game]
  (let [action (:action ast)
        action-handler (:handler action)
        room-key (:location @game)
        room (room/get-room room-key)]
    ;; FIXME: When multiple objects in play (like "get all"), every object involved needs to be given a chance
    ;;        to handle the action
    (cond
      (handle-errors ast game)
      true

      (handle-object-action :indirect-object ast game)
      true

      (handle-object-action :direct-object ast game)
      true

      action-handler
      (do (action-handler ast game)
          (when-let [room-fn (:fn room)]
            (room-fn ast game)))

      :else
      false)))
