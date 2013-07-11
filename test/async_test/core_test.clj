(ns async-test.core-test
  (:use midje.sweet)
  (:use clojure.core.async))



(defn set-table []
  (let [table [(chan) (chan) (chan) (chan) (chan)]]
    (go (>! (table 0) :fork))
    (go (>! (table 1) :fork))
    (go (>! (table 2) :fork))
    (go (>! (table 3) :fork))
    (go (>! (table 4) :fork))
    table))
(def table (set-table))
(def philosophers {:socrates   [0 1]
                   :plato      [1 2]
                   :rich       [2 3]
                   :sussman    [3 4]
                   :stroustrup [4 0]})

(defn pickup [fork]
  (<! fork) (println :smth_more_easy_to_read))

(defn pickup-left [table philo]
  (pickup (table (first (philo philosophers)))))

(defn pickup-right [table philo]
  (pickup (table (second (philo philosophers)))))

(defn putdown [fork]
  (>! fork :fork))

(defn putdown-left [table philo]
  (putdown (table (first (philo philosophers)))))

(defn putdown-right [table philo]
  (putdown (table (second (philo philosophers)))))

(defn finish-eating [table philo]
  (putdown-right table philo)
  (putdown-left table philo))

(defn eat [table philo]
  (go  (pickup-left table philo)
       ;; check I have the fork
       (pickup-right table philo)
       ;; check philo has the forks!!!!
       (println "nomnomnom" philo))
;  (putdown-right table philo)
;  (putdown-left table philo)
  )

(fact "I'm broken"
  ;; (<!! (pickup ((set-table) 0))) => :fork
  ;; (<!! (let [t (set-table)]
  ;;        (pickup (t 0))
  ;;        (putdown (t 0))
  ;;        (pickup (t 0)))) => :fork
  ;; (eat (set-table) :plato) => identity
  ;; (println "test tarts here")
  (let [t (set-table)]
    (doall (map (fn [philo] (eat t philo)) (keys philosophers))))
  => :false
)
