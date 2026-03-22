(ns ifgame.rooms)

(declare north-room)

(def start-room {:name "Starting Room"
                 :desc "You are in the starting room."
                 :exits {"north" #'north-room}})

(def north-room {:name "North Room"
                 :desc "Your are in the north room."
                 :exits {"south" #'start-room}})


(def east-room {:name "East Room"
                :desc "This is the east room."
                :exits {"west", #'start-room}})
