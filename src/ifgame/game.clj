(ns ifgame.game
  (:require [ifgame.rooms :as rooms]
            [ifgame.room :as room]))

(defrecord Game [title headline author state turns location health])

(def state (atom {:title "Heidi"
                  :headline "A simple example"
                  :author "Rod Schmidt"
                  :state :starting  ; can be :in-progress, :over, :won, etc.
                  :turns 0
                  :location nil
                  :health 100}))

(defn location [game-state]
  (:location @game-state))

(defn set-location [game-state new-location]
  (swap! game-state assoc :location new-location))

(defn init []
  (if rooms/starting-location
    (set-location state #'rooms/starting-location)
    (set-location state #'rooms/darkness))
  (room/visit (location state)))

(defn title [game-state]
  (:title game-state))

(defn headline [game-state]
  (:headline game-state))

(defn set-quit [game-state]
  (assoc game-state :state :quit))

(defn update-state [game-state]
  ; Update turns and state
  (let [new-turns (inc (:turns @game-state))]
    (swap! game-state assoc :turns new-turns)
    (when (= (:state @game-state) :starting)
      (swap! game-state assoc :state :in-progress))))

(defn over? [game-state]
  (= (:state game-state) :quit))

