(ns ifgame.parser
  (:require [instaparse.core :as insta]
            [ifgame.object :as object]
            [ifgame.game :as game]
            [ifgame.room :as room]))

#_(def if-parser
    (insta/parser
      "<S> = COMMAND
     <COMMAND> = verb (<ARTICLE?> direct_object (preposition <ARTICLE?> indirect_object)?)?
     verb = DIRECTION | #'(exit|x|quit|q|go|get|take|use|open|look|l|pick up|\\w+)'
     direct_object = noun | adjective noun
     indirect_object = noun | adjective noun
     preposition = #'(on|with|to)'
     noun = DIRECTION | #'(key|door|sword|\\w+)'
     DIRECTION = #'(north|n|south|s|east|e|west|w|up|u|down|d|northeast|ne|northwest|nw|southeast|se|southwest|sw)'
     adjective = #'(gold|elvish|green|\\w+)'
     ARTICLE = 'a' | 'an' | 'the'"
      :auto-whitespace :standard))

(def ^:private if-parser
  (insta/parser
    "<S> = verb (<article?> word+ (preposition <article?> word+)?)?
     verb = #'\\w+'
     word = #'(?!(?:the|a|an)\\b)\\w+'
     preposition = 'on' | 'with' | 'to' | 'in' | 'by' | 'under'
     <article> = 'a' | 'an' | 'the'"
    :auto-whitespace :standard))

(def ^:private articles #{"the" "a" "an"})

(defn- classify [words {:keys [nouns adjectives]}]
  ;; The last noun-ish word is the head noun; everything before is adjectives.
  (let [words (remove articles words)
        tagged (map (fn [w]
                      {:word w
                       :noun? (contains? nouns w)
                       :adj?  (contains? adjectives w)})
                    words)
        noun-idx (->> tagged
                      (keep-indexed (fn [i t] (when (:noun? t) i)))
                      last)]
    (if noun-idx
      {:adjectives (map :word (take noun-idx tagged))
       :noun (:word (nth tagged noun-idx))
       :extras (map :word (drop (inc noun-idx) tagged))}
      {:error :no-known-noun :words words})))

(defn- normalize-verb [verb]
  (let [standard-verbs {"get"      "take"
                        "i"        "inventory"
                        "l"        "look"
                        "pick up"  "take"
                        "put down" "drop"
                        "q"        "quit"
                        "travel"   "go"
                        "walk"     "go"
                        "x"        "examine"}]
    (standard-verbs verb verb)))

(defn- parse-command [input vocab]
  (let [tree (if-parser input)
        verb (->> tree (filter #(and (vector? %) (= :verb (first %)))) first second)
        ;; Split words by whether they appear before or after the preposition
        parts (rest tree)
        prep-idx (->> parts
                      (keep-indexed (fn [i x]
                                      (when (and (vector? x) (= :preposition (first x))) i)))
                      first)
        words-of (fn [xs] (->> xs (filter #(and (vector? %) (= :word (first %)))) (map second)))
        [do-words io-words] (if prep-idx
                              [(words-of (take prep-idx parts))
                               (words-of (drop (inc prep-idx) parts))]
                              [(words-of parts) nil])]
    {:verb (normalize-verb verb)
     :direct-object (when (seq do-words) (classify do-words vocab))
     :preposition (when prep-idx (second (nth parts prep-idx)))
     :indirect-object (when (seq io-words) (classify io-words vocab))}))

(defn- directions []
  #{"n" "north" "s" "south" "e" "east" "w" "west"
    "u" "up" "d" "down"
    "ne" "northeast" "nw" "northwest" "se" "southeast" "sw" "southwest"})

(defn- build-vocab [game-state]
  ;; TODO: Add player's inventory eventually
  (let [location (game/location game-state)
        object-keys (room/objects location)
        names (mapcat object/get-names object-keys)
        adjs (mapcat object/get-adjectives object-keys)]
    (-> {}
        (into {:nouns (set (concat (directions) names))})
        (into {:adjectives (set adjs)}))))

(defn parse [str game-state]
  (let [vocab (build-vocab game-state)]
    (println "vocab:" vocab)
    (parse-command str vocab)))

;; Example of usage
(comment
  ;; vocab is dynamic based on the current state of the game
  (def vocab
    {:nouns #{"key" "door" "sword" "bird" "baby" "troll"}
     :adjectives #{"gold" "elvish" "green" "damn" "baby" "wooden"}})

  (parse-command "use the gold key on the wooden door" vocab))
