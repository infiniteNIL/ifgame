(ns ifgame.game
  (:require [ifgame.rooms :as rooms]
            [ifgame.room :as room]
            [ifgame.player :as player]))

(def state (atom {:title "On His Secret Service"
                  :author "Rod Schmidt"
                  :state :starting  ; can be :in-progress, :over, :won, etc.
                  :player {:health 100
                           :location #'rooms/start-room}}))

(defn init []
  (room/visit (player/location state)))

(defn title [game-state]
  (:title game-state))

(defn set-quit [game-state]
  (assoc game-state :state :quit))

(defn over? [game-state]
  (= (:state game-state) :quit))
