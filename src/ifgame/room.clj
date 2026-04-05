(ns ifgame.room)

(defrecord Room [name description
                 north south east west up down
                 northwest northeast southwest southeast
                 has])

#_(defn- exits-description [location]
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
  (str (:name @location) \newline (:description @location)))

(defn short-description [location]
  (:name @location))

(defn first-visit? [location]
  (= 1 (:visit-count @location 0)))

(defn description [location]
  (if (first-visit? location)
    (full-description location)
    (short-description location)))

(defn visit [location]
  (let [count (:visit-count @location 0)]
    (alter-var-root location assoc :visit-count (inc count))))