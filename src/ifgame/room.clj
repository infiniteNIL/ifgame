(ns ifgame.room)

(defn- describe-exits [location]
  (let [exits (keys (:exits location))
        exit-count (count exits)]
    (cond
      (zero? exit-count)
      (print "There are no exits")

      (= exit-count 1)
      (print "The only exit is to the" (first exits))

      (= exit-count 2)
      (print "There are exits to the" (first exits) "and" (second exits))

      :else
      (let [last-exit (last exits)
            exits-str (clojure.string/join ", " (butlast exits))]
        (print "There are exits to the" exits-str)
        (print ", and" last-exit))))
  (println "."))

(defn describe-location [location]
  (println (:name @location))
  (print (:desc @location))
  (print " ")
  (describe-exits @location))
