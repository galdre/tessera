(ns tessera.core-test
  (:require [tessera.core :as core]
            [tessera.protocols.status :as status]
            [cljs.test :as t :include-macros true]))

(t/deftest unit:delay*
  (let [side-effect (atom 0)
        thunk (fn [] (swap! side-effect inc) :done)
        delay (core/delay* thunk)]
    (testing "Unredeemed delay"
      (t/is (core/tessera? delay)
            "is a tessera.")
      (t/is (core/pending? delay)
            "is pending.")
      (t/is (not (core/failed? delay))
            "is not failed.")
      (t/is (not (core/succeeded? delay))
            "has not succeeded.")
      (t/is (core/redeemable? delay)
            "is redeemable.")
      (t/is (core/ready? delay)
            "is ready.")
      (t/is (= 0 @side-effect)
            "has not triggered side effects.")
      (t/testing "has a status that is"
        (let [status (core/status delay)]
          (t/is (not (status/blocked? status))
                "not blocked.")
          (t/is (status/pending? status)
                "is pending.")
          (t/is (not (status/revoked? status))
                "is not revoked.")
          (t/is (status/ready? status)
                "is ready.")
          (t/is (not (status/failed? status))
                "is not failed.")
          (t/is (not (status/succeeded? status))
                "is not succeeded.")))
      (t/is (= :done (core/redeem delay))
            "can be redeemed for the expected value."))
    (t/testing "Redeemed delay"
      (t/is (not (core/pending? delay))
            "is not pending.")
      (t/is (core/ready? delay)
            "is ready.")
      (t/is (= 1 @side-effect)
            "executed computation only once.")
      (t/testing "has a status that is"
        (let [status (core/status delay)]
          (t/is (not (status/blocked? status))
                "not blocked.")
          (t/is (not (status/pending? status))
                "is not pending.")
          (t/is (not (status/revoked? status))
                "is not revoked.")
          (t/is (status/ready? status)
                "is ready.")
          (t/is (not (status/failed? status))
                "is not failed.")
          (t/is (status/succeeded? status)
                "is succeeded.")))
      (t/testing "can be redeemed again without recomputing."
        (t/is (= :done (core/redeem delay)))
        (t/is (= 1 @side-effect))))))

(t/deftest unit:promise*
  (let [promise (core/promise*)]
    (t/testing "Unfulfilled promise"
      (t/is (core/tessera? promise)
            "is a tessera.")
      (t/is (core/pending? promise)
            "is pending.")
      (t/is (not (core/failed? promise))
            "is not failed.")
      (t/is (not (core/succeeded? promise))
            "has not succeeded.")
      (t/is (core/redeemable? promise)
            "is redeemable")
      (t/is (not (core/ready? promise))
            "is not ready.")
      (t/is (= ::core/error (core/redeem promise))
            "cannot be redeemed.") ; TODO
      (t/is (core/revokable? promise)
            "is revokable.")
      (t/is (not (core/revoked? promise))
            "is not (by default) revoked.")
      (t/is (core/deliverable? promise)
            "is deliverable."))
    (t/testing "Fulfilled promise"
      (t/is (true? (core/deliver promise ::delivered))
            "indicates success on delivery attempt.")
      (t/is (not (core/pending? promise))
            "is not pending.")
      (t/is (core/succeeded? promise)
            "has indeed succeeded.")
      (t/is (core/ready? promise)
            "is ready.")
      (t/is (= ::delivered (core/redeem promise))
            "returns correct value on redemption.")
      (t/testing "Cannot deliver a second time"
        (t/is (nil? (core/deliver promise ::delivered-again)))
        (t/is (= ::delivered (core/redeem promise)))))))

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
