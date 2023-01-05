(ns nextjournal.clerkboard.demo
  {:nextjournal.clerk/visibility {:code :hide}}
  (:require [nextjournal.clerk :as clerk]))
;; # Hello Clerkboard!
;; The final answer is:
(clerk/html [:h3 (inc 41)])

;; The right time is `(java.time.Instant/now)`:
(java.time.Instant/now)
