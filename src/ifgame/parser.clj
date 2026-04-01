(ns ifgame.parser
  (:require [instaparse.core :as insta]))

(def if-parser
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

(defn- to-map
  "Take the AST returned by Instaparse and create a map"
  [ast]
  (into {} (map (fn [[k v]]
                  [k (if (and (vector? v) (keyword? (first v)))
                       (to-map [v])
                       v)])
                ast)))
(defn parse [str]
  (to-map (if-parser str)))
