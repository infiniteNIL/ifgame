(ns ifgame.room
  (:require [clojure.string :as str]))

(defn- exits-description [location]
  (let [exits (keys (:exits location))
        exit-count (count exits)]
    (case exit-count
      0 "There are no exits."
      1 (str "The only exit is to the " (first exits) ".")
      2 (str "There are exits to the " (first exits) " and " (second exits) ".")
      (let [last-exit (last exits)
            exits-str (str/join ", " (butlast exits))]
        (str "There are exits to the " exits-str " and " last-exit ".")))))

(defn full-description [location]
  (str (:name @location) \newline (:desc @location) " " (exits-description @location)))

(defn short-description [location]
  (:name @location))

(defn first-visit? [location]
  (= 1 (:visit-count @location 0)))

(defn visit [location]
  (let [count (:visit-count @location 0)]
    (alter-var-root location assoc :visit-count (inc count))))