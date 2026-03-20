(ns ifgame.core
  (:gen-class))

(declare start-room)

(def north-room {:name "North Room"
                 :desc "Your are in the north room."
                 :exits {"south" #'start-room}})

(def start-room {:name "Starting Room"
                 :desc "You are in the starting room."
                 :exits {"north" #'north-room}})

(def east-room {:name "East Room"
                :desc "This is the east room."
                :exits {"west", #'start-room}})

(def game-state (atom {:title "On His Secret Service"
                       :author "Rod Schmidt"
                       :state :starting  ; can be :in-progress, :over, :won, etc.
                       :player {:health 100
                                :location #'start-room}}))

(defn describe-exits [location]
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

(defn get-command []
  (println)
  (print "> ")
  (flush)
  (read-line))

(defn quit-command? [cmd]
  (some #{cmd} '("quit" "q" "x" "exit")))

(defn set-game-quit [game-state]
  (assoc game-state :state :quit))

(defn verify-quit []
  (print "Are you sure? (Y/n) ")
  (flush)
  (let [answer (read-line)]
    (or (= answer "y")
        (= answer "Y")
        (= answer ""))))

(defn player-location [game-state]
  (get-in @game-state [:player :location]))

(defn set-player-location [game-state new-location]
  (assoc-in game-state [:player :location] new-location))

(defn go [direction-str game-state]
  (let [destination (get-in @(player-location game-state) [:exits direction-str])]
    (if (nil? destination)
      (println "You can't go that way.")
      (swap! game-state set-player-location destination))))

(defn process-command [cmd game-state]
  (cond
    (quit-command? cmd)
    (if (verify-quit)
      (swap! game-state set-game-quit))

    (or (= cmd "n") (= cmd "north"))
    (go "north" game-state)

    (or (= cmd "s") (= cmd "south"))
    (go "south" game-state)

    :else
    (println "I don't know how to" cmd)))

(defn game-title [game-state]
  (:title game-state))

(defn print-title [game-state]
  (println (game-title game-state))
  (println "by" (:author game-state)))

(defn game-over? [game-state]
  (= (:state game-state) :quit))

(defn -main []
  (print-title @game-state)
  (println)
  (loop [prev-location nil]
    (when (not (game-over? @game-state))
      (describe-location (player-location game-state))
      (let [cmd (get-command)]
        (process-command cmd game-state)
        (recur (player-location game-state))))))