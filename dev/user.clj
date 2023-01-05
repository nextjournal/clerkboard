(ns user
  (:require [nextjournal.clerk :as clerk]))

(clerk/serve! {:port 7799 :browse true})
