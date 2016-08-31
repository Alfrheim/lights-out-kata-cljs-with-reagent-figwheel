(ns kata-lights-out.remote-lights
  (:require [kata-lights-out.lights :as lights-gateway]
            [com.stuartsierra.component :as component]
            [reagent.core :as r]
            [cljs-http.client :as http]
            [cljs.core.async :as async])
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]]))


(defn- extract-lights [response]
  (->> response
       :body
       (.parse js/JSON)
       .-lights
       js->clj))

(defn listen-to-lights-updates! [{:keys [lights-channel lights]}]
  (go-loop []
    (when-let [response (async/<! lights-channel)]
      (reset! lights (extract-lights response))
      (recur))))

(defrecord RemoteLights [config lights-channel]
  component/Lifecycle
  (start [this]
    (println ";; Starting lights component")
    (let [this (merge this {:lights-channel lights-channel
                            :lights (r/atom [])})]
      (listen-to-lights-updates! this)
      this))

  (stop [this]
    (println ";; Stopping lights component")
    this)

  lights-gateway/LightsGateway
  (reset-lights! [this m n]
    (async/pipe
      (http/post "http://localhost:3000/reset-lights"
                 {:with-credentials? false
                  :form-params {:m m :n n}})
      (:lights-channel this)
      false))

  (flip-light! [this [x y]]
    (async/pipe
      (http/post "http://localhost:3000/flip-light"
                 {:with-credentials? false
                  :form-params {:x x :y y}})
      (:lights-channel this)
      false)))

(defn make
  ""
  [config channel]
  (->RemoteLights config channel))
