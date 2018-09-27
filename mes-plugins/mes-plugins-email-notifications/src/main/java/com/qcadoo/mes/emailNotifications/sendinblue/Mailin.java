package com.qcadoo.mes.emailNotifications.sendinblue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.google.gson.Gson;

public class Mailin {

    private final String USER_AGENT = "Mozilla/5.0";

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private String base_url;

    private String api_key;

    public Mailin(String base_url, String api_key) {
        this.base_url = base_url;
        this.api_key = api_key;
    }

    public String do_request(String resource, String method, String input) throws Exception {
        String url = base_url + "/" + resource;
        String key = api_key;
        String content_header = "application/json";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestProperty("api-key", key);
        con.setRequestProperty("Content-Type", content_header);
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestMethod(method);
        con.setUseCaches(false);

        if (input != "" && method != "GET") {
            DataOutputStream outStream = new DataOutputStream(con.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream, "UTF-8"));
            writer.write(input);
            writer.flush();
            writer.close();
            outStream.close();
        }

        int responseCode = con.getResponseCode();
        String inputLine;
        StringBuffer response = new StringBuffer();
        BufferedReader in;

        if (200 <= responseCode && responseCode < 300) {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public String get(String resource, String input) {
        try {
            return do_request(resource, "GET", input);
        } catch (Exception e) {

        }
        return null;
    }

    public String put(String resource, String input) {
        try {
            return do_request(resource, "PUT", input);
        } catch (Exception e) {

        }
        return null;
    }

    public String post(String resource, String input) {
        try {
            return do_request(resource, "POST", input);
        } catch (Exception e) {

        }
        return null;
    }

    public String delete(String resource, String input) {
        try {
            return do_request(resource, "DELETE", input);
        } catch (Exception e) {

        }
        return null;
    }

    /*
     * Get Account. No input required
     */
    public String get_account() {
        return get("account", "");
    }

    /*
     * Get SMTP details. No input required
     */
    public String get_smtp_details() {
        return get("account/smtpdetail", "");
    }

    /*
     * Create Child Account.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} child_email: Email address of Reseller child [Mandatory]
     * @options data {String} password: Password of Reseller child to login [Mandatory]
     * @options data {String} company_org: Name of Reseller child’s company [Mandatory]
     * @options data {String} first_name: First name of Reseller child [Mandatory]
     * @options data {String} last_name: Last name of Reseller child [Mandatory]
     * @options data {Array} credits: Number of email & sms credits respectively, which will be assigned to the Reseller child’s
     * account [Optional] - email_credit {Integer} number of email credits - sms_credit {Integer} Number of sms credts
     * @options data {Array} associate_ip: Associate dedicated IPs to reseller child. You can use commas to separate multiple IPs
     * [Optional]
     */
    public String create_child_account(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("account", json);
    }

    /*
     * Update Child Account.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} auth_key: 16 character authorization key of Reseller child to be modified [Mandatory]
     * @options data {String} company_org: Name of Reseller child’s company [Optional]
     * @options data {String} first_name: First name of Reseller child [Optional]
     * @options data {String} last_name: Last name of Reseller child [Optional]
     * @options data {String} password: Password of Reseller child to login [Optional]
     * @options data {Array} associate_ip: Associate dedicated IPs to reseller child. You can use commas to separate multiple IPs
     * [Optional]
     * @options data {Array} disassociate_ip: Disassociate dedicated IPs from reseller child. You can use commas to separate
     * multiple IPs [Optional]
     */
    public String update_child_account(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return put("account", json);
    }

    /*
     * Delete Child Account.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} auth_key: 16 character authorization key of Reseller child to be deleted [Mandatory]
     */
    public String delete_child_account(Map<String, String> data) {
        String child_authkey = data.get("auth_key");
        return delete("account/" + child_authkey, "");
    }

    /*
     * Get Reseller child Account.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} auth_key: 16 character authorization key of Reseller child. Example : To get the details of more
     * than one child account, use, {"key1":"abC01De2fGHI3jkL","key2":"mnO45Pq6rSTU7vWX"} [Mandatory]
     */
    public String get_reseller_child(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("account/getchildv2", json);
    }

    /*
     * Add/Remove Reseller child's Email/Sms credits.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} auth_key: 16 character authorization key of Reseller child to modify credits [Mandatory]
     * @options data {Array} add_credit: Number of email & sms credits to be added. You can assign either email or sms credits,
     * one at a time other will remain 0. [Mandatory: if rmv_credit is empty] - email_credit {Integer} number of email credits -
     * sms_credit {Integer} Number of sms credts
     * @options data {Array} rmv_credit: Number of email & sms credits to be removed. You can assign either email or sms credits,
     * one at a time other will remain 0. [Mandatory: if add_credits is empty] - email_credit {Integer} number of email credits -
     * sms_credit {Integer} Number of sms credts
     */
    public String add_remove_child_credits(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("account/addrmvcredit", json);
    }

    /*
     * Send a transactional SMS.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} to: The mobile number to send SMS to with country code [Mandatory]
     * @options data {String} from: The name of the sender. The number of characters is limited to 11 (alphanumeric format)
     * [Mandatory]
     * @options data {String} text: The text of the message. The maximum characters used per SMS is 160, if used more than that,
     * it will be counted as more than one SMS [Mandatory]
     * @options data {String} web_url: The web URL that can be called once the message is successfully delivered [Optional]
     * @options data {String} tag: The tag that you can associate with the message [Optional]
     * @options data {String} type: Type of message. Possible values – marketing (default) & transactional. You can use marketing
     * for sending marketing SMS, & for sending transactional SMS, use transactional type [Optional]
     */
    public String send_sms(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("sms", json);
    }

    /*
     * Create & Schedule your SMS campaigns.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} name: Name of the SMS campaign [Mandatory]
     * @options data {String} sender: This allows you to customize the SMS sender. The number of characters is limited to 11 (
     * alphanumeric format ) [Optional]
     * @options data {String} content: Content of the message. The maximum characters used per SMS is 160, if used more than that,
     * it will be counted as more than one SMS [Optional]
     * @options data {String} bat: Mobile number with the country code to send test SMS. The mobile number defined here should
     * belong to one of your contacts in SendinBlue account and should not be blacklisted [Optional]
     * @options data {Array} listid: These are the list ids to which the SMS campaign is sent [Mandatory: if scheduled_date is not
     * empty]
     * @options data {Array} exclude_list: These are the list ids which will be excluded from the SMS campaign [Optional]
     * @options data {String} scheduled_date: The day on which the SMS campaign is supposed to run [Optional]
     * @options data {Integer} send_now: Flag to send campaign now. Possible values = 0 (default) & 1. send_now = 0 means campaign
     * can’t be send now, & send_now = 1 means campaign ready to send now [Optional]
     */
    public String create_sms_campaign(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("sms", json);
    }

    /*
     * Update your SMS campaigns.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of the SMS campaign [Mandatory]
     * @options data {String} name: Name of the SMS campaign [Optional]
     * @options data {String} sender: This allows you to customize the SMS sender. The number of characters is limited to 11 (
     * alphanumeric format ) [Optional]
     * @options data {String} content: Content of the message. The maximum characters used per SMS is 160, if used more than that,
     * it will be counted as more than one SMS [Optional]
     * @options data {String} bat: Mobile number with the country code to send test SMS. The mobile number defined here should
     * belong to one of your contacts in SendinBlue account and should not be blacklisted [Optional]
     * @options data {Array} listid: hese are the list ids to which the SMS campaign is sent [Mandatory: if scheduled_date is not
     * empty]
     * @options data {Array} exclude_list: These are the list ids which will be excluded from the SMS campaign [Optional]
     * @options data {String} scheduled_date: The day on which the SMS campaign is supposed to run [Optional]
     * @options data {Integer} send_now: Flag to send campaign now. Possible values = 0 (default) & 1. send_now = 0 means campaign
     * can’t be send now, & send_now = 1 means campaign ready to send now [Optional]
     */
    public String update_sms_campaign(Map<String, Object> data) {
        String id = data.get("id").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return put("sms/" + id, json);
    }

    /*
     * Send a Test SMS.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of the SMS campaign [Mandatory]
     * @options data {String} to: Mobile number with the country code to send test SMS. The mobile number defined here should
     * belong to one of your contacts in SendinBlue account and should not be blacklisted [Mandatory]
     */
    public String send_bat_sms(Map<String, Object> data) {
        String id = data.get("id").toString();
        String to = data.get("to").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return get("sms/" + id + "/" + to, "");
    }

    /*
     * Get all campaigns detail.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} type: Type of campaign. Possible values – classic, trigger, sms, template ( case sensitive )
     * [Optional]
     * @options data {String} status: Status of campaign. Possible values – draft, sent, archive, queued, suspended, in_process,
     * temp_active, temp_inactive ( case sensitive ) [Optional]
     * @options data {Integer} page: Maximum number of records per request is 500, if there are more than 500 campaigns then you
     * can use this parameter to get next 500 results [Optional]
     * @options data {Integer} page_limit: This should be a valid number between 1-1000. If page limit is kept empty or >1000,
     * default is 500 [Optional]
     */
    public String get_campaigns_v2(Map<String, Object> data) {
        String type = data.get("type").toString();
        String status = data.get("status").toString();
        String page = data.get("page").toString();
        String page_limit = data.get("page_limit").toString();
        String url = "";
        if ((type != null && type.isEmpty()) && (status != null && status.isEmpty()) && (page != null && page.isEmpty())
                && (page_limit != null && page_limit.isEmpty())) {
            url = "campaign/detailsv2/";
        } else {
            url = "campaign/detailsv2/type/" + type + "/status/" + status + "/page/" + page + "/page_limit/" + page_limit + "/";
        }
        return get(url, "");
    }

    /*
     * Get a particular campaign detail.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Unique Id of the campaign [Mandatory]
     */
    public String get_campaign_v2(Map<String, Object> data) {
        String id = data.get("id").toString();
        return get("campaign/" + id + "/detailsv2/", "");
    }

    /*
     * Create and Schedule your campaigns. It returns the ID of the created campaign.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} category: Tag name of the campaign [Optional]
     * @options data {String} from_name: Sender name from which the campaign emails are sent [Mandatory: for Dedicated IP clients,
     * please make sure that the sender details are defined here, and in case of no sender, you can add them also via API & for
     * Shared IP clients, if sender exists]
     * @options data {String} name: Name of the campaign [Mandatory]
     * @options data {String} bat: Email address for test mail [Optional]
     * @options data {String} html_content: Body of the content. The HTML content field must have more than 10 characters
     * [Mandatory: if html_url is empty]
     * @options data {String} html_url: Url which content is the body of content [Mandatory: if html_content is empty]
     * @options data {Array} listid: These are the lists to which the campaign has been sent [Mandatory: if scheduled_date is not
     * empty]
     * @options data {String} scheduled_date: The day on which the campaign is supposed to run[Optional]
     * @options data {String} subject: Subject of the campaign [Mandatory]
     * @options data {String} from_email: Sender email from which the campaign emails are sent [Mandatory: for Dedicated IP
     * clients, please make sure that the sender details are defined here, and in case of no sender, you can add them also via API
     * & for Shared IP clients, if sender exists]
     * @options data {String} reply_to: The reply to email in the campaign emails [Optional]
     * @options data {String} to_field: This is to personalize the «To» Field. If you want to include the first name and last name
     * of your recipient, add [PRENOM] [NOM] To use the contact attributes here, these should already exist in SendinBlue account
     * [Optional]
     * @options data {Array} exclude_list: These are the lists which must be excluded from the campaign [Optional]
     * @options data {String} attachment_url: Provide the absolute url of the attachment [Optional]
     * @options data {Integer} inline_image: Status of inline image. Possible values = 0 (default) & 1. inline_image = 0 means
     * image can’t be embedded, & inline_image = 1 means image can be embedded, in the email [Optional]
     * @options data {Integer} mirror_active: Status of mirror links in campaign. Possible values = 0 & 1 (default). mirror_active
     * = 0 means mirror links are deactivated, & mirror_active = 1 means mirror links are activated, in the campaign [Optional]
     * @options data {Integer} send_now: Flag to send campaign now. Possible values = 0 (default) & 1. send_now = 0 means campaign
     * can’t be send now, & send_now = 1 means campaign ready to send now [Optional]
     */
    public String create_campaign(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("campaign", json);
    }

    /*
     * Update your campaign.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of campaign to be modified [Mandatory]
     * @options data {String} category: Tag name of the campaign [Optional]
     * @options data {String} from_name: Sender name from which the campaign emails are sent [Mandatory: for Dedicated IP clients,
     * please make sure that the sender details are defined here, and in case of no sender, you can add them also via API & for
     * Shared IP clients, if sender exists]
     * @options data {String} name: Name of the campaign [Optional]
     * @options data {String} bat: Email address for test mail [Optional]
     * @options data {String} html_content: Body of the content. The HTML content field must have more than 10 characters
     * [Optional]
     * @options data {String} html_url: Url which content is the body of content [Optional]
     * @options data {Array} listid These are the lists to which the campaign has been sent [Mandatory: if scheduled_date is not
     * empty]
     * @options data {String} scheduled_date: The day on which the campaign is supposed to run[Optional]
     * @options data {String} subject: Subject of the campaign.
     * @options data {String} from_email: Sender email from which the campaign emails are sent [Mandatory: for Dedicated IP
     * clients, please make sure that the sender details are defined here, and in case of no sender, you can add them also via API
     * & for Shared IP clients, if sender exists]
     * @options data {String} reply_to: The reply to email in the campaign emails [Optional]
     * @options data {String} to_field: This is to personalize the «To» Field. If you want to include the first name and last name
     * of your recipient, add [PRENOM] [NOM]. To use the contact attributes here, these should already exist in SendinBlue account
     * [Optional]
     * @options data {Array} exclude_list: These are the lists which must be excluded from the campaign [Optional]
     * @options data {String} attachment_url: Provide the absolute url of the attachment [Optional]
     * @options data {Integer} inline_image: Status of inline image. Possible values = 0 (default) & 1. inline_image = 0 means
     * image can’t be embedded, & inline_image = 1 means image can be embedded, in the email [Optional]
     * @options data {Integer} mirror_active: Status of mirror links in campaign. Possible values = 0 & 1 (default). mirror_active
     * = 0 means mirror links are deactivated, & mirror_active = 1 means mirror links are activated, in the campaign [Optional]
     * @options data {Integer} send_now: Flag to send campaign now. Possible values = 0 (default) & 1. send_now = 0 means campaign
     * can’t be send now, & send_now = 1 means campaign ready to send now [Optional]
     */
    public String update_campaign(Map<String, Object> data) {
        String id = data.get("id").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return put("campaign/" + id, json);
    }

    /*
     * Delete your campaigns.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of campaign to be deleted [Mandatory]
     */
    public String delete_campaign(Map<String, Object> data) {
        String id = data.get("id").toString();
        return delete("campaign/" + id, "");
    }

    /*
     * Send report of Sent and Archived campaign.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of campaign to send its report [Mandatory]
     * @options data {String} lang: Language of email content. Possible values – fr (default), en, es, it & pt [Optional]
     * @options data {String} email_subject: Message subject [Mandatory]
     * @options data {Array} email_to: Email address of the recipient(s). Example: "test@example.net". You can use commas to
     * separate multiple recipients [Mandatory]
     * @options data {String} email_content_type: Body of the message in text/HTML version. Possible values – text & html
     * [Mandatory]
     * @options data {Array} email_bcc: Same as email_to but for Bcc [Optional]
     * @options data {Array} email_cc: Same as email_to but for Cc [Optional]
     * @options data {String} email_body: Body of the message [Mandatory]
     */
    public String campaign_report_email(Map<String, Object> data) {
        String id = data.get("id").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("campaign/" + id + "/report", json);
    }

    /*
     * Export the recipients of a specified campaign.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of campaign to export its recipients [Mandatory]
     * @options data {String} notify_url: URL that will be called once the export process is finished [Mandatory]
     * @options data {String} type: Type of recipients. Possible values – all, non_clicker, non_opener, clicker, opener,
     * soft_bounces, hard_bounces & unsubscribes [Mandatory]
     */
    public String campaign_recipients_export(Map<String, Object> data) {
        String id = data.get("id").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("campaign/" + id + "/recipients", json);
    }

    /*
     * Send a Test Campaign.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of the campaign [Mandatory]
     * @options data {Array} emails: Email address of recipient(s) existing in the one of the lists & should not be blacklisted.
     * Example: "test@example.net". You can use commas to separate multiple recipients [Mandatory]
     */
    public String send_bat_email(Map<String, Object> data) {
        String id = data.get("id").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("campaign/" + id + "/test", json);
    }

    /*
     * Create and schedule your Trigger campaigns.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} category: Tag name of the campaign [Optional]
     * @options data {String} from_name: Sender name from which the campaign emails are sent [Mandatory: for Dedicated IP clients,
     * please make sure that the sender details are defined here, and in case of no sender, you can add them also via API & for
     * Shared IP clients, if sender exists]
     * @options data {String} trigger_name: Name of the campaign [Mandatory]
     * @options data {String} bat: Email address for test mail [Optional]
     * @options data {String} html_content: Body of the content. The HTML content field must have more than 10 characters
     * [Mandatory: if html_url is empty]
     * @options data {String} html_url: Url which content is the body of content [Mandatory: if html_content is empty]
     * @options data {Array} listid: These are the lists to which the campaign has been sent [Mandatory: if scheduled_date is not
     * empty]
     * @options data {String} scheduled_date: The day on which the campaign is supposed to run[Optional]
     * @options data {String} subject: Subject of the campaign [Mandatory]
     * @options data {String} from_email: Sender email from which the campaign emails are sent [Mandatory: for Dedicated IP
     * clients, please make sure that the sender details are defined here, and in case of no sender, you can add them also via API
     * & for Shared IP clients, if sender exists]
     * @options data {String} reply_to: The reply to email in the campaign emails [Optional]
     * @options data {String} to_field: This is to personalize the «To» Field. If you want to include the first name and last name
     * of your recipient, add [PRENOM] [NOM]. To use the contact attributes here, these should already exist in SendinBlue account
     * [Optional]
     * @options data {Array} exclude_list: These are the lists which must be excluded from the campaign [Optional]
     * @options data {Integer} recurring: Type of trigger campaign. Possible values = 0 (default) & 1. recurring = 0 means contact
     * can receive the same Trigger campaign only once, & recurring = 1 means contact can receive the same Trigger campaign
     * several times [Optional]
     * @options data {String} attachment_url: Provide the absolute url of the attachment [Optional]
     * @options data {Integer} inline_image: Status of inline image. Possible values = 0 (default) & 1. inline_image = 0 means
     * image can’t be embedded, & inline_image = 1 means image can be embedded, in the email [Optional]
     * @options data {Integer} mirror_active: Status of mirror links in campaign. Possible values = 0 & 1 (default). mirror_active
     * = 0 means mirror links are deactivated, & mirror_active = 1 means mirror links are activated, in the campaign [Optional]
     * @options data {Integer} send_now: Flag to send campaign now. Possible values = 0 (default) & 1. send_now = 0 means campaign
     * can’t be send now, & send_now = 1 means campaign ready to send now [Optional]
     */
    public String create_trigger_campaign(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("campaign", json);
    }

    /*
     * Update and schedule your Trigger campaigns.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of Trigger campaign to be modified [Mandatory]
     * @options data {String} category: Tag name of the campaign [Optional]
     * @options data {String} from_name: Sender name from which the campaign emails are sent [Mandatory: for Dedicated IP clients,
     * please make sure that the sender details are defined here, and in case of no sender, you can add them also via API & for
     * Shared IP clients, if sender exists]
     * @options data {String} trigger_name: Name of the campaign [Mandatory]
     * @options data {String} bat Email address for test mail [Optional]
     * @options data {String} html_content: Body of the content. The HTML content field must have more than 10 characters
     * [Mandatory: if html_url is empty]
     * @options data {String} html_url: Url which content is the body of content [Mandatory: if html_content is empty]
     * @options data {Array} listid: These are the lists to which the campaign has been sent [Mandatory: if scheduled_date is not
     * empty]
     * @options data {String} scheduled_date: The day on which the campaign is supposed to run[Optional]
     * @options data {String} subject: Subject of the campaign [Mandatory]
     * @options data {String} from_email: Sender email from which the campaign emails are sent [Mandatory: for Dedicated IP
     * clients, please make sure that the sender details are defined here, and in case of no sender, you can add them also via API
     * & for Shared IP clients, if sender exists]
     * @options data {String} reply_to: The reply to email in the campaign emails [Optional]
     * @options data {String} to_field: This is to personalize the «To» Field. If you want to include the first name and last name
     * of your recipient, add [PRENOM] [NOM]. To use the contact attributes here, these should already exist in SendinBlue account
     * [Optional]
     * @options data {Array} exclude_list: These are the lists which must be excluded from the campaign [Optional]
     * @options data {Integer} recurring: Type of trigger campaign. Possible values = 0 (default) & 1. recurring = 0 means contact
     * can receive the same Trigger campaign only once, & recurring = 1 means contact can receive the same Trigger campaign
     * several times [Optional]
     * @options data {String} attachment_url: Provide the absolute url of the attachment [Optional]
     * @options data {Integer} inline_image: Status of inline image. Possible values = 0 (default) & 1. inline_image = 0 means
     * image can’t be embedded, & inline_image = 1 means image can be embedded, in the email [Optional]
     * @options data {Integer} mirror_active: Status of mirror links in campaign. Possible values = 0 & 1 (default). mirror_active
     * = 0 means mirror links are deactivated, & mirror_active = 1 means mirror links are activated, in the campaign [Optional]
     * @options data {Integer} send_now: Flag to send campaign now. Possible values = 0 (default) & 1. send_now = 0 means campaign
     * can’t be send now, & send_now = 1 means campaign ready to send now [Optional]
     */
    public String update_trigger_campaign(Map<String, Object> data) {
        String id = data.get("id").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return put("campaign/" + id, json);
    }

    /*
     * Get the Campaign name, subject and share link of the classic type campaigns only which are sent, for those which are not
     * sent and the rest of campaign types like trigger, template & sms, will return an error message of share link not available.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Array} camp_ids: Id of campaign to get share link. You can use commas to separate multiple ids [Mandatory]
     */
    public String share_campaign(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("campaign/sharelinkv2", json);
    }

    /*
     * Update the Campaign status.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of campaign to update its status [Mandatory]
     * @options data {String} status: Types of status. Possible values – suspended, archive, darchive, sent, queued, replicate and
     * replicate_template ( case sensitive ) [Mandatory]
     */
    public String update_campaign_status(Map<String, Object> data) {
        String id = data.get("id").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return put("campaign/" + id + "/updatecampstatus", json);
    }

    /*
     * Get all the processes information under the account.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} page: Maximum number of records per request is 50, if there are more than 50 processes then you can
     * use this parameter to get next 50 results [Mandatory]
     * @options data {Integer} page_limit: This should be a valid number between 1-50 [Mandatory]
     */
    public String get_processes(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return get("process", json);
    }

    /*
     * Get the process information.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of process to get details [Mandatory]
     */
    public String get_process(Map<String, Object> data) {
        String id = data.get("id").toString();
        return get("process/" + id, "");
    }

    /*
     * Get all lists detail.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} list_parent: This is the existing folder id & can be used to get all lists belonging to it
     * [Optional]
     * @options data {Integer} page: Maximum number of records per request is 50, if there are more than 50 processes then you can
     * use this parameter to get next 50 results [Mandatory]
     * @options data {Integer} page_limit: This should be a valid number between 1-50 [Mandatory]
     */
    public String get_lists(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return get("list", json);
    }

    /*
     * Get a particular list detail.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of list to get details [Mandatory]
     */
    public String get_list(Map<String, Object> data) {
        String id = data.get("id").toString();
        return get("list/" + id, "");
    }

    /*
     * Create a new list.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} list_name: Desired name of the list to be created [Mandatory]
     * @options data {Integer} list_parent: Folder ID [Mandatory]
     */
    public String create_list(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("list", json);
    }

    /*
     * Delete a specific list.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of list to be deleted [Mandatory]
     */
    public String delete_list(Map<String, Object> data) {
        String id = data.get("id").toString();
        return delete("list/" + id, "");
    }

    /*
     * Update a list.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of list to be modified [Mandatory]
     * @options data {String} list_name: Desired name of the list to be modified [Optional]
     * @options data {Integer} list_parent: Folder ID [Mandatory]
     */
    public String update_list(Map<String, Object> data) {
        String id = data.get("id").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return put("list/" + id, json);
    }

    /*
     * Display details of all users for the given lists.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Array} listids: These are the list ids to get their data. The ids found will display records [Mandatory]
     * @options data {String} timestamp: This is date-time filter to fetch modified user records >= this time. Valid format Y-m-d
     * H:i:s. Example: "2015-05-22 14:30:00" [Optional]
     * @options data {Integer} page: Maximum number of records per request is 500, if in your list there are more than 500 users
     * then you can use this parameter to get next 500 results [Optional]
     * @options data {Integer} page_limit: This should be a valid number between 1-500 [Optional]
     */
    public String display_list_users(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("list/display", json);
    }

    /*
     * Add already existing users in the SendinBlue contacts to the list.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of list to link users in it [Mandatory]
     * @options data {Array} users: Email address of the already existing user(s) in the SendinBlue contacts. Example:
     * "test@example.net". You can use commas to separate multiple users [Mandatory]
     */
    public String add_users_list(Map<String, Object> data) {
        String id = data.get("id").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("list/" + id + "/users", json);
    }

    /*
     * Delete already existing users in the SendinBlue contacts from the list.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of list to unlink users from it [Mandatory]
     * @options data {Array} users: Email address of the already existing user(s) in the SendinBlue contacts to be modified.
     * Example: "test@example.net". You can use commas to separate multiple users [Mandatory]
     */
    public String delete_users_list(Map<String, Object> data) {
        String id = data.get("id").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return delete("list/" + id + "/delusers", json);
    }

    /*
     * Send Transactional Email.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Array} to: Email address of the recipient(s). It should be sent as an associative array. Example:
     * array("to@example.net"=>"to whom"). You can use commas to separate multiple recipients [Mandatory]
     * @options data {String} subject: Message subject [Mandatory]
     * @options data {Array} from Email address for From header. It should be sent as an array. Example: array("from@email.com",
     * "from email") [Mandatory]
     * @options data {String} html: Body of the message. (HTML version) [Mandatory]. To send inline images, use <img
     * src="{YourFileName.Extension}" alt="image" border="0" >, the 'src' attribute value inside {} (curly braces) should be same
     * as the filename used in 'inline_image' parameter
     * @options data {String} text: Body of the message. (text version) [Optional]
     * @options data {Array} cc: Same as to but for Cc. Example: array("cc@example.net","cc whom") [Optional]
     * @options data {Array} bcc: Same as to but for Bcc. Example: array("bcc@example.net","bcc whom") [Optional]
     * @options data {Array} replyto: Same as from but for Reply To. Example: array("from@email.com","from email") [Optional]
     * @options data {Array} attachment: Provide the absolute url of the attachment/s. Possible extension values = gif, png, bmp,
     * cgm, jpg, jpeg, txt, css, shtml, html, htm, csv, zip, pdf, xml, doc, xls, ppt, tar and ez. To send attachment/s generated
     * on the fly you have to pass your attachment/s filename & its base64 encoded chunk data as an associative array. Example:
     * array("YourFileName.Extension"=>"Base64EncodedChunkData"). You can use commas to separate multiple attachments [Optional]
     * @options data {Array} headers: The headers will be sent along with the mail headers in original email. Example:
     * array("Content-Type"=>"text/html; charset=iso-8859-1"). You can use commas to separate multiple headers [Optional]
     * @options data {Array} inline_image: Pass your inline image/s filename & its base64 encoded chunk data as an associative
     * array. Possible extension values = gif, png, bmp, cgm, jpg and jpeg. Example:
     * array("YourFileName.Extension"=>"Base64EncodedChunkData"). You can use commas to separate multiple inline images [Optional]
     */
    public String send_email(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("email", json);
    }

    /*
     * To retrieve details of all webhooks.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} is_plat: Flag to get webhooks. Possible values – 0 & 1. Example: to get Transactional webhooks, use
     * $is_plat=0, to get Marketing webhooks, use $is_plat=1, & to get all webhooks, use $is_plat="" [Optional]
     */
    public String get_webhooks(Map<String, String> data) {
        String is_plat = data.get("is_plat");
        String url = "";
        if (is_plat != null && is_plat.isEmpty()) {
            url = "webhook/";
        } else {
            url = "webhook/is_plat/" + is_plat + "/";
        }
        return get(url, "");
    }

    /*
     * To retrieve details of any particular webhook.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of webhook to get details [Mandatory]
     */
    public String get_webhook(Map<String, Object> data) {
        String id = data.get("id").toString();
        return get("webhook/" + id, "");
    }

    /*
     * Create a Webhook.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} url: URL that will be triggered by a webhook [Mandatory]
     * @options data {String} description: Webook description [Optional]
     * @options data {Array} events: Set of events. You can use commas to separate multiple events. Possible values for
     * Transcational webhook – request, delivered, hard_bounce, soft_bounce, blocked, spam, invalid_email, deferred, click, &
     * opened and Possible Values for Marketing webhook – spam, opened, click, hard_bounce, unsubscribe, soft_bounce &
     * list_addition ( case sensitive ) [Mandatory]
     * @options data {Integer} is_plat: Flag to create webhook type. Possible values – 0 (default) & 1. Example: to create
     * Transactional webhooks, use $is_plat=0, & to create Marketing webhooks, use $is_plat=1 [Optional]
     */
    public String create_webhook(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("webhook", json);
    }

    /*
     * Delete a webhook.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of webhook to be deleted [Mandatory]
     */
    public String delete_webhook(Map<String, Object> data) {
        String id = data.get("id").toString();
        return delete("webhook/" + id + "/", "");
    }

    /*
     * Update a webhook.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of webhook to be modified [Mandatory]
     * @options data {String} url: URL that will be triggered by a webhook [Mandatory]
     * @options data {String} description: Webook description [Optional]
     * @options data {Array} events: Set of events. You can use commas to separate multiple events. Possible values for
     * Transcational webhook – request, delivered, hard_bounce, soft_bounce, blocked, spam, invalid_email, deferred, click, &
     * opened and Possible Values for Marketing webhook – spam, opened, click, hard_bounce, unsubscribe, soft_bounce &
     * list_addition ( case sensitive ) [Mandatory]
     */
    public String update_webhook(Map<String, Object> data) {
        String id = data.get("id").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return put("webhook/" + id, json);
    }

    /*
     * Aggregate / date-wise report of the SendinBlue SMTP account.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} aggregate: This is used to indicate, you are interested in all-time totals. Possible values – 0 &
     * 1. aggregate = 0 means it will not aggregate records, and will show stats per day/date wise [Optional]
     * @options data {String} start_date: The start date to look up statistics. Date must be in YYYY-MM-DD format and should be
     * before the end_date [Optional]
     * @options data {String} end_date: The end date to look up statistics. Date must be in YYYY-MM-DD format and should be after
     * the start_date [Optional]
     * @options data {Integer} days: Number of days in the past to include statistics ( Includes today ). It must be an integer
     * greater than 0 [Optional]
     * @options data {String} tag: The tag you will specify to retrieve detailed stats. It must be an existing tag that has
     * statistics [Optional]
     */
    public String get_statistics(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("statistics", json);
    }

    /*
     * Create a new user if an email provided as input, doesn’t exists in the contact list of your SendinBlue account, otherwise
     * it will update the existing user.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} email: Email address of the user to be created in SendinBlue contacts. Already existing email
     * address of user in the SendinBlue contacts to be modified [Mandatory]
     * @options data {Array} attributes: The name of attribute present in your SendinBlue account. It should be sent as an
     * associative array. Example: array("NAME"=>"name"). You can use commas to separate multiple attributes [Optional]
     * @options data {Integer} blacklisted: This is used to blacklist/ Unblacklist a user. Possible values – 0 & 1. blacklisted =
     * 1 means user has been blacklisted [Optional]
     * @options data {Array} listid: The list id(s) to be linked from user [Optional]
     * @options data {Array} listid_unlink: The list id(s) to be unlinked from user [Optional]
     * @options data {Array} blacklisted_sms: This is used to blacklist/ Unblacklist a user’s SMS number. Possible values – 0 & 1.
     * blacklisted_sms = 1 means user’s SMS number has been blacklisted [Optional]
     */
    public String create_update_user(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("user/createdituser", json);
    }

    /*
     * Get Access a specific user Information.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} email: Email address of the already existing user in the SendinBlue contacts [Mandatory]
     */
    public String get_user(Map<String, String> data) {
        String id = data.get("email");
        return get("user/" + id, "");
    }

    /*
     * Unlink existing user from all lists.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} email: Email address of the already existing user in the SendinBlue contacts to be unlinked from all
     * lists [Mandatory]
     */
    public String delete_user(Map<String, String> data) {
        String id = data.get("email");
        return delete("user/" + id, "");
    }

    /*
     * Import Users Information.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} url: The URL of the file to be imported. Possible file types – .txt, .csv [Mandatory: if body is
     * empty]
     * @options data {String} body: The Body with csv content to be imported. Example:
     * ‘NAME;SURNAME;EMAIL\n"Name1";"Surname1";"example1@example.net"\n"Name2";"Surname2";"example2@example.net"‘, where \n
     * separates each user data. You can use semicolon to separate multiple attributes [Mandatory: if url is empty]
     * @options data {Array} listids: These are the list ids in which the the users will be imported [Mandatory: if name is empty]
     * @options data {String} notify_url: URL that will be called once the import process is finished [Optional] In notify_url, we
     * are sending the content using POST method
     * @options data {String} name: This is new list name which will be created first & then users will be imported in it
     * [Mandatory: if listids is empty]
     * @options data {Integer} list_parent: This is the existing folder id & can be used with name parameter to make newly created
     * list’s desired parent [Optional]
     */
    public String import_users(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("user/import", json);
    }

    /*
     * Export Users Information.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} export_attrib: The name of attribute present in your SendinBlue account. You can use commas to
     * separate multiple attributes. Example: "EMAIL,NAME,SMS" [Optional]
     * @options data {String} filter: Filter can be added to export users. Example: "{\"blacklisted\":1}", will export all
     * blacklisted users [Mandatory]
     * @options data {String} notify_url: URL that will be called once the export process is finished [Optional]
     */
    public String export_users(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("user/export", json);
    }

    /*
     * Access all the attributes information under the account. No input required
     */
    public String get_attributes() {
        return get("attribute", "");
    }

    /*
     * Access the specific type of attribute information.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} type: Type of attribute. Possible values – normal, transactional, category, calculated & global
     * [Optional]
     */
    public String get_attribute(Map<String, String> data) {
        String type = data.get("type");
        return get("attribute/" + type, "");
    }

    /*
     * Create an Attribute.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} type: Type of attribute. Possible values – normal, transactional, category, calculated & global (
     * case sensitive ) [Mandatory]
     * @options data {Array} data: The name and data type of ‘normal’ & ‘transactional’ attribute to be created in your SendinBlue
     * account. It should be sent as an associative array. Example: array(‘ATTRIBUTE_NAME1′ => ‘DATA_TYPE1′, ‘ATTRIBUTE_NAME2’=>
     * ‘DATA_TYPE2′). The name and data value of ‘category’, ‘calculated’ & ‘global’, should be sent as JSON string. Example: ‘[{
     * "name":"ATTRIBUTE_NAME1", "value":"Attribute_value1" }, { "name":"ATTRIBUTE_NAME2", "value":"Attribute_value2" }]’. You can
     * use commas to separate multiple attributes [Mandatory]
     */
    public String create_attribute(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("attribute", json);
    }

    /*
     * Delete a specific type of attribute information.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} type: Type of attribute to be deleted [Mandatory]
     */
    public String delete_attribute(Map<String, Object> data) {
        String type = data.get("type").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("attribute/" + type, json);
    }

    /*
     * Get Email Event report.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} limit: To limit the number of results returned. It should be an integer [Optional]
     * @options data {String} start_date: The start date to get report from. Date must be in YYYY-MM-DD format and should be
     * before the end_date [Optional]
     * @options data {String} end_date: The end date to get report till date. Date must be in YYYY-MM-DD format and should be
     * after the start_date [Optional]
     * @options data {Integer} offset: Beginning point in the list to retrieve from. It should be an integer [Optional]
     * @options data {String} date: Specific date to get its report. Date must be in YYYY-MM-DD format and should be earlier than
     * todays date [Optional]
     * @options data {Integer} days: Number of days in the past (includes today). If specified, must be an integer greater than 0
     * [Optional]
     * @options data {String} email: Email address to search report for [Optional]
     */
    public String get_report(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("report", json);
    }

    /*
     * Get all folders detail.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} page: Maximum number of records per request is 50, if there are more than 50 folders then you can
     * use this parameter to get next 50 results [Mandatory]
     * @options data {Integer} page_limit: This should be a valid number between 1-50 [Mandatory]
     */
    public String get_folders(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return get("folder", json);
    }

    /*
     * Get a particular folder detail.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of folder to get details [Mandatory]
     */
    public String get_folder(Map<String, Object> data) {
        String id = data.get("id").toString();
        return get("folder/" + id, "");
    }

    /*
     * Create a new folder.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} name: Desired name of the folder to be created [Mandatory]
     */
    public String create_folder(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("folder", json);
    }

    /*
     * Delete a specific folder information.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of folder to be deleted [Mandatory]
     */
    public String delete_folder(Map<String, Object> data) {
        String id = data.get("id").toString();
        return delete("folder/" + id, "");
    }

    /*
     * Update an existing folder.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of folder to be modified [Mandatory]
     * @options data {String} name: Desired name of the folder to be modified [Mandatory]
     */
    public String update_folder(Map<String, Object> data) {
        String id = data.get("id").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return put("folder/" + id, json);
    }

    /*
     * Delete any hardbounce, which actually would have been blocked due to some temporary ISP failures.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} start_date: The start date to get report from. Date must be in YYYY-MM-DD format and should be
     * before the end_date [Optional]
     * @options data {String} end_date: The end date to get report till date. Date must be in YYYY-MM-DD format and should be
     * after the start_date [Optional]
     * @options data {String} email: Email address to delete its bounces [Optional]
     */
    public String delete_bounces(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("bounces", json);
    }

    /*
     * Send templates created on SendinBlue, through SendinBlue SMTP (transactional mails).
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of the template created on SendinBlue account [Mandatory]
     * @options data {String} to: Email address of the recipient(s). You can use pipe ( | ) to separate multiple recipients.
     * Example: "to-example@example.net|to2-example@example.net" [Mandatory]
     * @options data {String} cc: Same as to but for Cc [Optional]
     * @options data {String} bcc: Same as to but for Bcc [Optional]
     * @options data {Array} attr: The name of attribute present in your SendinBlue account. It should be sent as an associative
     * array. Example: array("NAME"=>"name"). You can use commas to separate multiple attributes [Optional]
     * @options data {String} attachment_url: Provide the absolute url of the attachment. Url not allowed from local machine. File
     * must be hosted somewhere [Optional]
     * @options data {Array} attachment: To send attachment/s generated on the fly you have to pass your attachment/s filename &
     * its base64 encoded chunk data as an associative array [Optional]
     * @options data {Array} headers: The headers will be sent along with the mail headers in original email. Example:
     * array("Content-Type"=>"text/html; charset=iso-8859-1"). You can use commas to separate multiple headers [Optional]
     */
    public String send_transactional_template(Map<String, Object> data) {
        String id = data.get("id").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return put("template/" + id, json);
    }

    /*
     * Create a Template.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} from_name: Sender name from which the campaign emails are sent [Mandatory: for Dedicated IP clients
     * & for Shared IP clients, if sender exists]
     * @options data {String} template_name: Name of the Template [Mandatory]
     * @options data {String} bat: Email address for test mail [Optional]
     * @options data {String} html_content: Body of the content. The HTML content field must have more than 10 characters
     * [Mandatory: if html_url is empty]
     * @options data {String} html_url Url: which content is the body of content [Mandatory: if html_content is empty]
     * @options data {String} subject: Subject of the campaign [Mandatory]
     * @options data {String} from_email: Sender email from which the campaign emails are sent [Mandatory: for Dedicated IP
     * clients & for Shared IP clients, if sender exists]
     * @options data {String} reply_to: The reply to email in the campaign emails [Optional]
     * @options data {String} to_fieldv This is to personalize the «To» Field. If you want to include the first name and last name
     * of your recipient, add [PRENOM] [NOM]. To use the contact attributes here, these should already exist in SendinBlue account
     * [Optional]
     * @options data {Integer} status: Status of template. Possible values = 0 (default) & 1. status = 0 means template is
     * inactive, & status = 1 means template is active [Optional]
     * @options data {Integer} attachment: Status of attachment. Possible values = 0 (default) & 1. attach = 0 means an attachment
     * can’t be sent, & attach = 1 means an attachment can be sent, in the email [Optional]
     */
    public String create_template(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("template", json);
    }

    /*
     * Update a Template.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of Template to be modified [Mandatory]
     * @options data {String} from_name: Sender name from which the campaign emails are sent [Mandatory: for Dedicated IP clients
     * & for Shared IP clients, if sender exists]
     * @options data {String} template_name: Name of the Template [Mandatory]
     * @options data {String} bat: Email address for test mail [Optional]
     * @options data {String} html_content: Body of the content. The HTML content field must have more than 10 characters
     * [Mandatory: if html_url is empty]
     * @options data {String} html_url: Url which content is the body of content [Mandatory: if html_content is empty]
     * @options data {String} subject: Subject of the campaign [Mandatory]
     * @options data {String} from_email: Sender email from which the campaign emails are sent [Mandatory: for Dedicated IP
     * clients & for Shared IP clients, if sender exists]
     * @options data {String} reply_to: The reply to email in the campaign emails [Optional]
     * @options data {String} to_field: This is to personalize the «To» Field. If you want to include the first name and last name
     * of your recipient, add [PRENOM] [NOM]. To use the contact attributes here, these should already exist in SendinBlue account
     * [Optional]
     * @options data {Integer} status: Status of template. Possible values = 0 (default) & 1. status = 0 means template is
     * inactive, & status = 1 means template is active [Optional]
     * @options data {Integer} attachment: Status of attachment. Possible values = 0 (default) & 1. attach = 0 means an attachment
     * can’t be sent, & attach = 1 means an attachment can be sent, in the email [Optional]
     */
    public String update_template(Map<String, Object> data) {
        String id = data.get("id").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return put("template/" + id, json);
    }

    /*
     * Get Access of created senders information.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} option: Options to get senders. Possible options – IP-wise, & Domain-wise ( only for dedicated IP
     * clients ). Example: to get senders with specific IP, use $option=’1.2.3.4′, to get senders with specific domain use,
     * $option=’domain.com’, & to get all senders, use $option="" [Optional]
     */
    public String get_senders(Map<String, String> data) {
        String option = data.get("option");
        String url = "";
        if (option != null && option.isEmpty()) {
            url = "advanced/";
        } else {
            url = "advanced/option/" + option + "/";
        }
        return get(url, "");
    }

    /*
     * Create your Senders.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {String} name: Name of the sender [Mandatory]
     * @options data {String} email: Email address of the sender [Mandatory]
     * @options data {Array} ip_domain: Pass pipe ( | ) separated Dedicated IP and its associated Domain. Example:
     * "1.2.3.4|mydomain.com". You can use commas to separate multiple ip_domain’s [Mandatory: Only for Dedicated IP clients, for
     * Shared IP clients, it should be kept blank]
     */
    public String create_sender(Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return post("advanced", json);
    }

    /*
     * Update your Senders.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of sender to be modified [Mandatory]
     * @options data {String} name: Name of the sender [Mandatory]
     * @options data {Array} ip_domain: Pass pipe ( | ) separated Dedicated IP and its associated Domain. Example:
     * "1.2.3.4|mydomain.com". You can use commas to separate multiple ip_domain’s [Mandatory: Only for Dedicated IP clients, for
     * Shared IP clients, it should be kept blank]
     */
    public String update_sender(Map<String, Object> data) {
        String id = data.get("id").toString();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        return put("advanced/" + id, json);
    }

    /*
     * Delete your Sender Information.
     * @param {Object} data contains json objects as a key value pair from HashMap.
     * @options data {Integer} id: Id of sender to be deleted [Mandatory]
     */
    public String delete_sender(Map<String, Object> data) {
        String id = data.get("id").toString();
        return delete("advanced/" + id, "");
    }
}