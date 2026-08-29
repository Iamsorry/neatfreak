---
layout: default
title: NeatFreak Privacy Policy
permalink: /privacy-policy/
---

# NeatFreak（連潔癖）Privacy Policy／隱私權政策

[繁體中文](#繁體中文) | [English](#english)

若不同語言版本之間有任何歧義，以繁體中文版本為準。

If there is any discrepancy between language versions, the Traditional Chinese version prevails.

## 繁體中文

最後更新日期：2026 年 8 月 29 日

本隱私權政策適用於由 Iamsorry（以下稱「開發者」）提供的 Android 應用程式 NeatFreak（中文名稱「連潔癖」，以下稱「本 App」；套件名稱：`io.github.iamsorry.neatfreak`）。本 App 用於整理使用者提供的網頁連結，包括解析部分短連結或分享連結的轉址、移除追蹤參數，以及讓使用者複製、分享或開啟處理後的連結。

### 資料存取與處理

本 App 會依使用者主動進行的操作，處理下列內容：

- 使用者直接輸入或貼上的文字與網址。
- 使用者透過 Android 分享功能傳送至本 App 的文字與網址。
- 使用者透過 Android「處理文字」功能交給本 App 的所選文字與網址。
- 使用者按下「貼上」按鈕時，Android 系統剪貼簿中的文字內容。

上述內容僅用於找出、解析及清理使用者指定的網址。本 App 不會讀取剪貼簿中的其他項目，也不會在使用者未按下「貼上」按鈕時主動讀取剪貼簿。

本 App 不要求建立帳號，也不存取通訊錄、位置、相片、相機、麥克風、裝置識別碼或廣告識別碼。

### 網路連線與第三方服務

多數網址清理工作會直接在使用者的裝置上完成。為解析部分 Threads、Facebook 與 LinkedIn 短連結或分享連結的實際目的地，本 App 會從使用者的裝置直接向該網址及其轉址目的地發出 HTTP 或 HTTPS 請求。本 App 不會先將這些網址傳送至開發者所營運的伺服器。

收到請求的網站、網路服務供應商或其他中介服務可能取得該次連線通常會附帶的資訊，例如：

- 使用者提供的完整請求網址；該網址本身可能包含由原網站加入的識別資訊或參數。
- IP 位址及連線時間。
- 本 App 使用的 User-Agent 資訊。
- 服務正常運作與安全防護所需的其他技術資料。

這些第三方如何蒐集、使用、保存或分享資料，受各第三方自己的隱私權政策約束，開發者無法控制其處理方式。依目前支援的連結類型，可能涉及 Meta Platforms, Inc. 經營的 Threads、Facebook 與 Instagram，以及 LinkedIn、Spotify、Valve（Steam）、Google（YouTube）、Amazon 或使用者提供網址所指向的其他網站。只有需要解析轉址的連結會在清理過程中由本 App 主動提出網路請求；單純移除網址參數不需要連線至該網站。

### 複製、分享與開啟連結

- 當使用者選擇複製，或從 Android 分享功能啟動預設的「清理並複製」流程時，本 App 會將處理後的網址寫入 Android 系統剪貼簿。剪貼簿內容後續由 Android 系統及使用者裝置上的其他 App 依其各自權限處理。
- 當使用者選擇分享時，本 App 會把處理後的網址交給 Android 系統分享介面；只有使用者選定的接收 App 才會收到該網址。
- 當使用者選擇開啟時，本 App 會把處理後的網址交給能處理該網址的其他 App，例如瀏覽器或對應服務的官方 App。
- 使用 Android「處理文字」功能且原始文字欄位允許編輯時，本 App 可將處理後的網址回傳給提出要求的 App。

資料一旦交由 Android 系統、網站或其他 App，將依該系統、網站或 App 的隱私權政策處理。

### 開發者不蒐集或分享的資料

本 App 目前未整合廣告、使用情形分析、追蹤、遙測或崩潰回報服務。開發者不會透過本 App 將使用者輸入的內容、網址、剪貼簿內容或使用紀錄傳送至開發者控制的伺服器，也不會出售或基於廣告目的分享這些資料。

本 App 使用 Android Jetpack、Kotlin Coroutines 與 OkHttp 等程式庫提供介面、非同步處理及網路連線功能；目前版本未將這些程式庫設定為向開發者回傳分析或追蹤資料。

### 資料保存與刪除

本 App 不建立使用者帳號，也不在開發者控制的伺服器保存使用者資料。輸入內容、解析結果及錯誤訊息僅保存在 App 執行期間的記憶體中，本 App 未將其寫入本機資料庫、偏好設定或檔案；當相關畫面或 App 程序結束後，這些執行期間資料即不再由本 App 保存。因此，開發者沒有可供個別查詢或刪除的伺服器端使用者資料。

本 App 寫入系統剪貼簿的內容由 Android 管理。使用者可隨時複製其他內容以取代它；部分 Android 版本也可能自動清除剪貼簿內容。已傳送至第三方網站或其他 App 的資料，須依該第三方提供的方式提出存取或刪除要求。

### 資料安全

本 App 將資料處理範圍限制在提供連結清理功能所需的最低程度，不營運用來接收或保存使用者網址的後端服務。解析轉址時，本 App 會限制轉址次數，拒絕不安全的轉址目的地，並阻擋指向本機或私人網路位址的轉址，以降低非預期連線的風險。

然而，任何網路傳輸或裝置端處理方式均無法保證絕對安全。使用者不應將含有密碼、存取權杖、私人文件連結或其他敏感資訊的網址交給本 App，除非使用者了解該網址可能會被傳送至其所指向的第三方網站。

### 兒童隱私

本 App 並非以兒童為主要對象，開發者也不會蓄意蒐集兒童的個人資料。由於開發者不營運接收本 App 使用者資料的伺服器，若使用者認為第三方網站可能已取得兒童資料，應直接聯絡該網站的營運者。

### 政策變更

本政策可能因本 App 功能、所用服務或法規要求變更而更新。更新後的版本會公布於同一網址，並修改頁首的「最後更新日期」。如變更會實質影響使用者資料的處理方式，開發者將透過適當方式提供更明確的通知。

### 聯絡方式

如對本隱私權政策或本 App 的資料處理方式有疑問，可透過 GitHub Issues 聯絡開發者：

<https://github.com/iamsorry/neatfreak/issues>

---

## English

Last updated: August 29, 2026

This Privacy Policy applies to NeatFreak (Chinese name: "連潔癖"; package name: `io.github.iamsorry.neatfreak`), an Android application provided by Iamsorry (the "Developer"). The App organizes web links supplied by users. Its functions include resolving redirects for certain shortened or shared links, removing tracking parameters, and allowing users to copy, share, or open the processed links.

### Data Access and Processing

The App processes the following content in response to actions initiated by the user:

- Text and URLs entered or pasted directly by the user.
- Text and URLs sent to the App through the Android sharing feature.
- Selected text and URLs passed to the App through Android's Process Text feature.
- Text in the Android system clipboard when the user taps the Paste button.

This content is used solely to locate, resolve, and clean the URL specified by the user. The App does not read other clipboard items and does not proactively read the clipboard unless the user taps the Paste button.

The App does not require an account and does not access contacts, location, photos, the camera, the microphone, device identifiers, or advertising identifiers.

### Network Connections and Third-Party Services

Most URL-cleaning operations are performed directly on the user's device. To determine the actual destinations of certain Threads, Facebook, and LinkedIn shortened or shared links, the App sends HTTP or HTTPS requests directly from the user's device to the supplied URL and its redirect destinations. The App does not first send these URLs to a server operated by the Developer.

Websites receiving these requests, network service providers, or other intermediaries may receive information normally associated with the connection, including:

- The complete requested URL supplied by the user, which may itself contain identifying information or parameters added by the originating website.
- The IP address and connection time.
- The User-Agent information used by the App.
- Other technical information necessary for service operation and security.

The collection, use, retention, and sharing of data by these third parties are governed by their respective privacy policies, and the Developer does not control their practices. Depending on the currently supported link type, the relevant third parties may include Threads, Facebook, and Instagram, operated by Meta Platforms, Inc.; LinkedIn; Spotify; Valve (Steam); Google (YouTube); Amazon; or another website identified by a URL supplied by the user. During cleaning, the App actively makes network requests only for links that require redirect resolution. Merely removing URL parameters does not require a connection to the website.

### Copying, Sharing, and Opening Links

- When the user chooses to copy a link, or starts the default Clean and Copy flow from Android's sharing feature, the App writes the processed URL to the Android system clipboard. Android and other apps on the user's device may subsequently handle clipboard contents according to their respective permissions.
- When the user chooses to share a link, the App passes the processed URL to the Android system sharing interface. Only the receiving app selected by the user receives the URL.
- When the user chooses to open a link, the App passes the processed URL to another app capable of handling it, such as a web browser or the relevant service's official app.
- When Android's Process Text feature is used with an editable source text field, the App may return the processed URL to the app that made the request.

Once data has been passed to Android, a website, or another app, it is handled under the privacy policy of that system, website, or app.

### Data the Developer Does Not Collect or Share

The App currently does not integrate advertising, usage analytics, tracking, telemetry, or crash-reporting services. The Developer does not use the App to send user-entered content, URLs, clipboard contents, or usage records to a server controlled by the Developer. The Developer does not sell this data or share it for advertising purposes.

The App uses libraries such as Android Jetpack, Kotlin Coroutines, and OkHttp to provide its interface, asynchronous processing, and network functionality. The current version does not configure these libraries to send analytics or tracking data back to the Developer.

### Data Retention and Deletion

The App does not create user accounts or store user data on servers controlled by the Developer. Input, resolution results, and error messages are held only in memory while the App is running. The App does not write this information to a local database, preferences, or files. When the relevant screen or App process ends, the App no longer retains this runtime data. The Developer therefore has no server-side user data that can be individually retrieved or deleted.

Content written by the App to the system clipboard is managed by Android. The user may replace it at any time by copying other content, and some Android versions may also clear clipboard contents automatically. Requests to access or delete data already sent to a third-party website or another app must be made using the methods provided by that third party.

### Data Security

The App limits data processing to what is necessary to provide its link-cleaning functions and does not operate a backend service that receives or stores user URLs. When resolving redirects, the App limits the number of redirects, rejects unsafe redirect destinations, and blocks redirects to local or private-network addresses to reduce the risk of unintended connections.

However, no method of network transmission or on-device processing can be guaranteed to be absolutely secure. Users should not provide the App with URLs containing passwords, access tokens, private document links, or other sensitive information unless they understand that the URL may be sent to the third-party website it identifies.

### Children's Privacy

The App is not directed primarily at children, and the Developer does not knowingly collect children's personal data. Because the Developer does not operate a server that receives user data from the App, users who believe that a third-party website may have received a child's data should contact the operator of that website directly.

### Changes to This Policy

This Policy may be updated in response to changes in the App's features, the services it uses, or applicable legal requirements. The updated version will be published at the same URL, and the Last updated date at the beginning of the English section will be revised. If a change materially affects how user data is processed, the Developer will provide more prominent notice through an appropriate method.

### Contact

For questions about this Privacy Policy or the App's data-handling practices, contact the Developer through GitHub Issues:

<https://github.com/iamsorry/neatfreak/issues>
