# Getting Started

### Application Usage

For further reference, please consider the following sections:

* run maven install
* run application
* In browser go to: http://localhost:8080/home.html
* Upload CSV file in as follows:
*
  |  url |
  |---|
  | 1a.ee  |
  | kaubamaja.ee  |
* Press Button "IMPORT"
* Wait until a page http://localhost:8080/success pops up. It means that processing ended
* In browser go to: http://localhost:8080/report
* Accept proposed CSV file

## CSV file

###CSV fields:
* prospectID - ID in H2 database
* companyName -  Official Name of the company
* prospectEmail - Email scraped from website. Can be used as contact information
* active - Status of website. All disabled websites were sorted out during the scraping process.
* platform - website platform
* companyId - Official Company name in the Country(Äriregister ID)
* websiteurl - website url used for scraping
* contactdata - contact number.  Can be used as contact information

## Database
* In this project used H2 database 
* Can be accessed via browser link: http://localhost:8080/h2-console/
* JDBC URL: jdbc:h2:mem:testdb
* User Name: sa
* Password:	password

## Third-Party Api
* For a platform loo-up used https://whatcms.org/
* NB max 1000 usages allowed. Use another free account, then always response always none
* Every website URL takes 10 sec in order to optimize speed need to write custom CMS look-up or upgrade to paid API version


