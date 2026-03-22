(ns ifgame.player)

(defn location [game-state]
  (get-in @game-state [:player :location]))

(defn set-location [game-state new-location]
  (assoc-in game-state [:player :location] new-location))
