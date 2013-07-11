(ns async-test.core-test
  (:use async-test.core)
  (:use midje.sweet))

(fact "I'm broken"
  (pickup ((set-table) 0)) => :fork
  (let [t (set-table)]
     (pickup (t 0))
     (putdown (t 0))
     (pickup (t 0))) => :fork
  (eat (set-table) :plato) => nil
  (let [t (set-table)]
    (for [p (rest (keys philosophers))]
      (thread (eat t p))))
  => #(doall (map <!! %))
)
