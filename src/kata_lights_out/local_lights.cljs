(ns kata-lights-out.local-lights
  (:require [com.stuartsierra.component :as component]
            [kata-lights-out.lights :as lights-gateway]
            [reagent.core :as r]))



(def ^:private light-on 1)
(def ^:private light-off 0)

(defn- neighbors? [[i0 j0] [i j]]
  (or (and (= j0 j) (= 1 (Math/abs (- i0 i))))
      (and (= i0 i) (= 1 (Math/abs (- j0 j))))))

(defn- neighbors [m n pos]
  (for [i (range m)
        j (range n)
        :when (neighbors? pos [i j])]
    [i j]))

(defn- flip-light [light]
  (if (lights-gateway/light-off? light)
    light-on
    light-off))

(defn- flip [lights pos]
  (update-in lights pos flip-light))

(defn- flip-neighbors [m n pos lights]
  (->> pos
       (neighbors m n)
       (cons pos)
       (reduce flip lights)))

(defn- all-lights-on [m n]
  (vec (repeat m (vec (repeat n light-on)))))

(defn- num-rows [lights]
  (count lights))

(defn- num-colums [lights]
  (count (first lights)))

(defrecord LocalLightsGateway
    []

  component/Lifecycle
  (start [this]
    (println ";; Starting lights in local component")
    (assoc this :lights (r/atom [])))

  (stop [this]
    (println ";; Stopping lights in local component")
    this)

  lights-gateway/LightsGateway
  (reset-lights! [{:keys [lights]} m n]
    (reset! lights (all-lights-on m n)))
  (flip-light!
      [{:keys [lights]} pos]
    (swap! lights (partial flip-neighbors (num-rows @lights) (num-colums @lights) pos))))

(defn make
  "Factory we build things"
  []
  (->LocalLightsGateway))

