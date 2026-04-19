(ns com.example.main
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [reitit.ring :as ring]
            [com.example.pages.plain :as plain]
            [com.example.pages.htmx :as htmx]
            [com.example.pages.datastar :as datastar])
  (:gen-class))

(def state (atom {:items (vec (for [i (range 1 51)]
                                {:name (str "Item " i) :quantity 0}))
                  :settings {:text-field ""
                             :select-field "option-a"
                             :date-field ""}}))

(def app
  (ring/ring-handler
   (ring/router
    [["/" {:get {:handler (fn [_] {:status 302 :headers {"Location" "/plain"}})}}]
     ["/plain" {:get {:handler (fn [req] (plain/page req state))}}]
     ["/plain/save" {:post {:handler (fn [req] (plain/save! req state))}}]
     ["/plain/increment" {:post {:handler (fn [req] (plain/increment! req state))}}]
     ["/plain/decrement" {:post {:handler (fn [req] (plain/decrement! req state))}}]
     ["/plain/settings" {:post {:handler (fn [req] (plain/save-settings! req state))}}]
     ["/htmx" {:get {:handler (fn [req] (htmx/page req state))}}]
     ["/htmx/save" {:post {:handler (fn [req] (htmx/save! req state))}}]
     ["/htmx/increment" {:post {:handler (fn [req] (htmx/increment! req state))}}]
     ["/htmx/decrement" {:post {:handler (fn [req] (htmx/decrement! req state))}}]
     ["/htmx/settings" {:post {:handler (fn [req] (htmx/save-settings! req state))}}]
     ["/datastar" {:get {:handler (fn [req] (datastar/page req state))}}]
     ["/datastar/save" {:post {:handler (fn [req] (datastar/save! req state))}}]
     ["/datastar/increment" {:post {:handler (fn [req] (datastar/increment! req state))}}]
     ["/datastar/decrement" {:post {:handler (fn [req] (datastar/decrement! req state))}}]
     ["/datastar/settings" {:post {:handler (fn [req] (datastar/save-settings! req state))}}]
     ["/reset" {:post {:handler (fn [_]
                                  (reset! state {:items (vec (for [i (range 1 51)]
                                                              {:name (str "Item " i) :quantity 0}))
                                                 :settings {:text-field ""
                                                            :select-field "option-a"
                                                            :date-field ""}})
                                  {:status 200 :body "ok"})}}]])
   (ring/create-default-handler)
   {:middleware [wrap-params wrap-multipart-params]}))

(defn -main [& _args]
  (println "Starting server on port 8080...")
  (jetty/run-jetty app {:port 8080 :join? true}))
