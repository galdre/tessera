(ns tessera.core-test
  (:require [tessera.core :as core]
            [cljs.test :as t :include-macros true]))

(t/deftest unit:delay*
  (let [side-effect (atom 0)
        thunk (fn [] (swap! side-effect inc) :done)
        delay (core/delay* thunk)]
    (t/is (core/tessera? delay))
    (t/is (core/pending? delay))
    (t/is (not (core/failed? delay)))
    (t/is (not (core/succeeded? delay)))
    (t/is (core/redeemable? delay))
    (t/is (core/ready? delay))
    (t/is (= 0 @side-effect))
    (t/is (= :done (core/redeem delay)))
    (t/is (not (core/pending? delay)))
    (t/is (core/ready? delay))
    (t/is (= 1 @side-effect))
    (t/is (= :done (core/redeem delay)))
    (t/is (= 1 @side-effect))))

(t/deftest unit:promise*
  (let [promise (core/promise*)
        test-pending (fn [promise]
                       (t/testing "Pre-fulfilled promise"
                         (t/is (core/tessera? promise))
                         (t/is (core/pending? promise))
                         (t/is (not (core/failed? promise)))
                         (t/is (not (core/succeeded? promise)))
                         (t/is (core/redeemable? promise))
                         (t/is (not (core/ready? promise)))
                         (t/is (= ::core/error (core/redeem promise))) ; TODO
                         (t/is (core/revokable? promise))
                         (t/is (not (core/revoked? promise)))
                         (t/is (core/deliverable? promise))))]
    (test-pending promise)
    ;; Deliver!
    (t/testing "Delivered promise"
      (t/is (true? (core/deliver promise ::delivered)))
      (t/is (not (core/pending? promise)))
      (t/is (core/succeeded? promise))
      (t/is (core/ready? promise))
      (t/is (= ::delivered (core/redeem promise))))))

(t/deftest unit:watcher*
  (t/testing "Watching a delay"
    (let [side-effect (atom nil)
          f (fn [status value]
              (reset! side-effect [status value]))
          delay (core/delay* #(inc 4))]
      (core/watch* delay f)
      (t/is (nil? @side-effect))
      (t/is (= 5 (core/redeem delay)))
      (t/is (= 5 (second @side-effect)))))
  (t/testing "Watching a promise"
    (let [side-effect (atom nil)
          f (fn [status value]
              (reset! side-effect [status value]))
          promise (core/promise*)]
      (core/watch* promise f)
      (t/is (nil? @side-effect))
      (t/is (true? (core/deliver promise 11)))
      (t/is (= 11 (second @side-effect))))))
