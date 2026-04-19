(ns com.example.pages.datastar
  (:require [com.example.ui :as ui]
            [clojure.string :as str]
            [lambdaisland.hiccup :as hiccup]))

(defn ds-items-table [items]
  (let [save-btn (fn [] [:button {:type "button"
                                  :data-on-click "@post('/datastar/save', {contentType:'form'})"
                                  :style "margin: 10px 0; padding: 5px 20px;"}
                         "Save"])]
    [:div
     (save-btn)
     [:table {:border "1" :cellpadding "5" :cellspacing "0" :style "border-collapse: collapse;"}
      [:thead
       [:tr [:th "Name"] [:th "Quantity"] [:th "Actions"]]]
      [:tbody
       (for [[i item] (map-indexed vector items)]
         [:tr
          [:td (:name item)]
          [:td [:input {:type "text" :name (str "qty-" i)
                        :value (str (:quantity item))
                        :style "width: 60px; text-align: center;"}]]
          [:td
           [:button {:type "button"
                     :data-on-click (str "@post('/datastar/increment?index=" i "', {contentType:'form'})")
                     :style "margin-right: 5px;"} "+"]
           [:button {:type "button"
                     :data-on-click (str "@post('/datastar/decrement?index=" i "', {contentType:'form'})")} "-"]]])]]
     (save-btn)]))

(defn ds-settings-form [settings]
  [:div {:style "margin-top: 30px; padding: 20px; border: 1px solid #ccc;"}
   [:h2 "Settings"]
   [:div {:style "margin-bottom: 10px;"}
    [:label {:for "text-field"} "Text field: "]
    [:input {:type "text" :id "text-field" :name "text-field"
             :value (:text-field settings)}]]
   [:div {:style "margin-bottom: 10px;"}
    [:label {:for "select-field"} "Select field: "]
    [:select {:id "select-field" :name "select-field"}
     (for [opt ["option-a" "option-b" "option-c"]]
       [:option (merge {:value opt}
                       (when (= opt (:select-field settings))
                         {:selected "selected"}))
        opt])]]
   [:div {:style "margin-bottom: 10px;"}
    [:label {:for "date-field"} "Date: "]
    [:input {:type "date" :id "date-field" :name "date-field"
             :value (:date-field settings)}]]
   [:button {:type "button"
             :data-on-click "@post('/datastar/settings', {contentType:'form'})"
             :style "padding: 5px 20px;"} "Save Settings"]])

(defn render-body [items settings]
  [:div {:id "main-content" :style "font-family: sans-serif; padding: 20px;"}
   (ui/nav-bar)
   [:h1 "Datastar"]
   [:form {:id "main-form"}
    (ds-items-table items)
    (ds-settings-form settings)]])

(defn page [_req state]
  (let [{:keys [items settings]} @state]
    (ui/render-html
     [:html
      [:head
       [:title "Datastar"]
       [:script {:type "module" :src "https://cdn.jsdelivr.net/npm/@starfederation/datastar@1.0.0-beta.11/dist/datastar.min.js"}]]
      [:body
       (render-body items settings)]])))

(defn render-fragment [state]
  (let [{:keys [items settings]} @state
        html (hiccup/render (render-body items settings) {})
        html (str/replace html #"^<!DOCTYPE html>\n?" "")
        ;; SSE format: each line of data must be prefixed with "data: "
        data-lines (str/split (str "fragments " html) #"\n")
        sse-data (str/join "\n" (map #(str "data: " %) data-lines))
        sse (str "event: datastar-merge-fragments\n" sse-data "\n\n")]
    {:status 200
     :headers {"Content-Type" "text/event-stream"
               "Cache-Control" "no-cache"}
     :body sse}))

(defn save! [req state]
  (let [params (:params req)]
    (swap! state update :items
           (fn [items]
             (vec (for [i (range (count items))]
                    (assoc (nth items i) :quantity
                           (ui/parse-int (get params (str "qty-" i))))))))
    (render-fragment state)))

(defn increment! [req state]
  (let [idx (ui/parse-int (get-in req [:params "index"]))]
    (swap! state update-in [:items idx :quantity] inc)
    (render-fragment state)))

(defn decrement! [req state]
  (let [idx (ui/parse-int (get-in req [:params "index"]))]
    (swap! state update-in [:items idx :quantity] dec)
    (render-fragment state)))

(defn save-settings! [req state]
  (let [params (:params req)]
    (swap! state assoc :settings
           {:text-field (get params "text-field" "")
            :select-field (get params "select-field" "option-a")
            :date-field (get params "date-field" "")})
    (render-fragment state)))
