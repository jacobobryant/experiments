(ns com.example.ui
  (:require [lambdaisland.hiccup :as hiccup]))

(defn render-html [hiccup-form]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (hiccup/render hiccup-form {})})

(defn redirect [path]
  {:status 303
   :headers {"Location" path}})

(defn parse-int [s]
  (try (Integer/parseInt (str s))
       (catch Exception _ 0)))

(defn nav-bar []
  [:nav {:style "margin-bottom: 20px; padding: 10px; background: #f0f0f0;"}
   [:a {:href "/plain" :style "margin-right: 15px;"} "Plain HTML"]
   [:a {:href "/htmx" :style "margin-right: 15px;"} "HTMX + Idiomorph"]
   [:a {:href "/datastar"} "Datastar"]])

(defn items-table [{:keys [items action-prefix save-action attrs]}]
  (let [save-btn (fn [] [:button (merge {:type "submit" :formaction save-action
                                         :style "margin: 10px 0; padding: 5px 20px;"}
                                        attrs)
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
          [:td [:input (merge {:type "text" :name (str "qty-" i)
                               :value (str (:quantity item))
                               :style "width: 60px; text-align: center;"})]]
          [:td
           [:button (merge {:type "submit" :formaction (str action-prefix "/increment")
                            :name "index" :value (str i)
                            :style "margin-right: 5px;"}
                           attrs) "+"]
           [:button (merge {:type "submit" :formaction (str action-prefix "/decrement")
                            :name "index" :value (str i)}
                           attrs) "-"]]])]]
     (save-btn)]))

(defn settings-form [{:keys [settings action attrs]}]
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
   [:button (merge {:type "submit" :formaction action
                    :style "padding: 5px 20px;"}
                   attrs) "Save Settings"]])
