(ns com.example.pages.plain
  (:require [com.example.ui :as ui]))

(defn page [_req state]
  (let [{:keys [items settings]} @state]
    (ui/render-html
     [:html
      [:head [:title "Plain HTML Forms"]]
      [:body {:style "font-family: sans-serif; padding: 20px;"}
       (ui/nav-bar)
       [:h1 "Plain HTML Forms"]
       [:form {:method "post"}
        (ui/items-table {:items items
                         :action-prefix "/plain"
                         :save-action "/plain/save"})
        (ui/settings-form {:settings settings
                           :action "/plain/settings"})]]])))

(defn save! [req state]
  (let [params (:params req)]
    (swap! state update :items
           (fn [items]
             (vec (for [i (range (count items))]
                    (assoc (nth items i) :quantity
                           (ui/parse-int (get params (str "qty-" i))))))))
    (ui/redirect "/plain")))

(defn increment! [req state]
  (let [idx (ui/parse-int (get-in req [:params "index"]))]
    (swap! state update-in [:items idx :quantity] inc)
    (ui/redirect "/plain")))

(defn decrement! [req state]
  (let [idx (ui/parse-int (get-in req [:params "index"]))]
    (swap! state update-in [:items idx :quantity] dec)
    (ui/redirect "/plain")))

(defn save-settings! [req state]
  (let [params (:params req)]
    (swap! state assoc :settings
           {:text-field (get params "text-field" "")
            :select-field (get params "select-field" "option-a")
            :date-field (get params "date-field" "")})
    (ui/redirect "/plain")))
