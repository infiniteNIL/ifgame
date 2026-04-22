(ns ifgame.game
  (:require [ifgame.room :as room]))

;(defrecord Game [title headline author state turns location health])

(defn create [title headline author start-location]
  (atom {:title title
         :headline headline
         :author author
         :state :starting
         :turns 0
         :location start-location
         :health 100}))

(defn set-location [game new-location]
  (swap! game assoc :location new-location))

(defn init [game]
  (room/visit (:location @game)))

(defn set-quit [game-state]
  (assoc game-state :state :quit))

(defn update-game [game]
  ; Update turns and state
  (let [new-turns (inc (:turns @game))]
    (swap! game assoc :turns new-turns)
    (when (= (:state @game) :starting)
      (swap! game assoc :state :in-progress))))

(defn over? [game-state]
  (= (:state game-state) :quit))

(defn add-to-inventory [game-state object-key]
  (let [new-inventory (conj (:inventory @game-state) object-key)]
    (swap! game-state assoc :inventory new-inventory)))

(defn remove-from-inventory [game-state object-key]
  (let [new-inventory (remove #{object-key} (:inventory @game-state))]
    (swap! game-state assoc :inventory new-inventory)))

(defn in-inventory? [game object-key]
  (some #{object-key} (:inventory @game)))
