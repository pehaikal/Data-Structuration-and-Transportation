# Data Structuration and Transportation

## Context
You oversee developing a batch to send emails automatically for a company marketing campaigns.
The marketing campaigns are targeting inhabitants of specific cities, but unfortunately the marketing tool used is not able to send emails automatically nor to observe tracking of the emails sent. On another hand, you have already a bot that does this, but needs specific data structuration to achieve that.

## Global Project Schema:
![Data Structuration and Transportation](https://user-images.githubusercontent.com/72979397/195388344-334763ad-1de8-43f5-ade0-4c0bb4b127e0.png)

## Batch Implementation Model:
![Batch Implementation](https://user-images.githubusercontent.com/72979397/195388463-ba2da1c1-028b-49d1-a638-96deda6a69df.png)

## Tasks
This project is split in several tasks, to facilitate your evaluation.
1. Choose a framework among:
    a- Camel (Java)
    b- Bonobo (python)

2. Load the contacts.sql in the database (postgres is preferred or h2 for java users).

3.	Prepare a pipeline that will:
    - Read the contacts items from the database
    - For each contact selected (thanks to the campaign meta),  create an email body (html format) from the provided email template, each wildcard should be replaced by the appropriate contact info.

    - Example: in the resulting file :
    '<td class="hero-subheader__title" style="font-size: 43px; font-weight: bold; padding: 80px 0 15px 0;" align="left">A special offer for you in your city of ${city}</td>'

    - Will become:
    '<td class="hero-subheader__title" style="font-size: 43px; font-weight: bold; padding: 80px 0 15px 0;" align="left">A special offer for you in your city of New York</td>'

    - If the current contact city field is ‘New York’
    a. Put the resulting email file in the output folder “email-spool” (can be anywhere on your file system)
    b. Prepare a daily html report of the email sent to what contact.
    c. Make everything configurable (no hardcoded conf entries)
    d. Make everything trackable (have the state of the contact processing; is it “selected for campaign”, “email ready”, “email sent”?) so that your pipeline will be able to not take the same contacts twice, even after a forceful stop.
