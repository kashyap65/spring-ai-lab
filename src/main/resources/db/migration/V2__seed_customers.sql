-- V2__seed_customers.sql
-- 50 realistic customers across all plans, statuses, login patterns
-- Designed to make AI queries interesting:
--   - Mix of active/inactive/churned
--   - Varying last_login (some 90+ days ago for churn detection)
--   - Rich notes field used for RAG semantic search

INSERT INTO customers
    (first_name, last_name, email, phone, plan, plan_start_date, plan_end_date,
     status, last_login_at, login_count, country, city,
     open_tickets, total_tickets, lifetime_value, monthly_spend, notes)
VALUES

-- PREMIUM customers (used heavily in AI query examples)
('Arjun',     'Sharma',    'arjun.sharma@techcorp.in',    '+91-9876543210', 'PREMIUM',    '2023-01-15', '2024-01-15', 'ACTIVE',    NOW() - INTERVAL '2 days',   145, 'India',         'Bengaluru', 0, 3,  4800.00, 400.00,
 'Senior engineer at TechCorp. Uses API integration heavily. Raised ticket about rate limiting in March. Very satisfied with premium support SLA.'),

('Priya',     'Patel',     'priya.patel@finserv.com',     '+91-9123456789', 'PREMIUM',    '2022-06-01', '2024-06-01', 'ACTIVE',    NOW() - INTERVAL '1 day',    310, 'India',         'Mumbai',    1, 12, 9600.00, 400.00,
 'Power user. Logs in daily. Uses analytics dashboard and bulk export features. Has referred 3 colleagues. Asked about enterprise plan pricing last month.'),

('James',     'Wilson',    'james.wilson@startup.io',     '+1-415-555-0101','PREMIUM',    '2023-03-10', '2024-03-10', 'ACTIVE',    NOW() - INTERVAL '45 days',  89,  'USA',           'San Francisco', 2, 8, 4000.00, 400.00,
 'CTO of a Series A startup. Has not logged in for 45 days -- potential churn risk. Last interaction was a support ticket about data export. Should be flagged for re-engagement.'),

('Sofia',     'Rossi',     'sofia.rossi@designstudio.it', '+39-02-5550123', 'PREMIUM',    '2023-07-20', '2024-07-20', 'ACTIVE',    NOW() - INTERVAL '3 days',   201, 'Italy',         'Milan',     0, 5,  4400.00, 400.00,
 'Design agency owner. Uses white-labelling features. Very vocal advocate on social media. Sent NPS score of 9. Interested in team collaboration features.'),

('Raj',       'Kumar',     'raj.kumar@ecommerce.in',      '+91-8888877777', 'PREMIUM',    '2022-11-01', '2024-11-01', 'ACTIVE',    NOW() - INTERVAL '7 days',   178, 'India',         'Delhi',     0, 6,  8000.00, 400.00,
 'E-commerce platform owner. Heavily uses customer segmentation and email automation features. Renewed premium plan twice. Potential enterprise upgrade candidate.'),

('Chen',      'Wei',       'chen.wei@trading.hk',         '+852-9876-5432', 'PREMIUM',    '2023-05-01', '2024-05-01', 'ACTIVE',    NOW() - INTERVAL '1 day',    267, 'Hong Kong',     'Hong Kong', 0, 2,  5200.00, 400.00,
 'Algorithmic trading firm. Uses real-time data features. Very sensitive to latency. Praised the new WebSocket implementation. Wants dedicated support channel.'),

('Emily',     'Johnson',   'emily.j@consulting.co.uk',    '+44-20-7946-0100','PREMIUM',   '2023-09-01', '2024-09-01', 'ACTIVE',    NOW() - INTERVAL '60 days',  44,  'UK',            'London',    3, 15, 3600.00, 400.00,
 'Management consultant. Login frequency dropped sharply 2 months ago. Has 3 open tickets unanswered. High churn risk. Previously a very active user -- something changed.'),

('Marco',     'Santos',    'marco.santos@logistics.br',   '+55-11-9999-0001','PREMIUM',   '2023-02-14', '2024-02-14', 'ACTIVE',    NOW() - INTERVAL '5 days',   133, 'Brazil',        'Sao Paulo', 0, 4,  5600.00, 400.00,
 'Logistics company. Uses route optimisation features. Requested Portuguese language support. Lifetime value growing steadily. Happy customer.'),

('Aisha',     'Mohammed',  'aisha.m@mediagroup.ae',       '+971-50-123-4567','PREMIUM',   '2023-04-01', '2024-04-01', 'ACTIVE',    NOW() - INTERVAL '2 days',   198, 'UAE',           'Dubai',     0, 3,  5200.00, 400.00,
 'Media group digital director. Uses content scheduling and analytics. Has a large team that also uses BASIC plan seats. Exploring enterprise upgrade.'),

('Lior',      'Ben-David', 'lior.b@cybersec.co.il',       '+972-52-888-9999','PREMIUM',   '2022-08-01', '2024-08-01', 'ACTIVE',    NOW() - INTERVAL '1 day',    423, 'Israel',        'Tel Aviv',  0, 7,  11200.00,400.00,
 'Cybersecurity firm. Among top 5 highest-engagement premium users. Has submitted 12 feature requests, 8 implemented. Strong product advocate. Enterprise upsell priority.'),

-- ENTERPRISE customers
('David',     'Park',      'david.park@bigbank.com',      '+1-212-555-0200', 'ENTERPRISE','2022-01-01', '2025-01-01', 'ACTIVE',    NOW() - INTERVAL '1 day',    612, 'USA',           'New York',  0, 22, 86400.00,2400.00,
 'VP Engineering at major bank. Compliance-sensitive. Uses audit logs and SSO extensively. Annual contract. Has dedicated CSM. Renewal due Jan 2025 -- high value.'),

('Tanaka',    'Hiroshi',   'h.tanaka@automaker.jp',       '+81-3-5555-1234', 'ENTERPRISE','2022-04-01', '2025-04-01', 'ACTIVE',    NOW() - INTERVAL '2 days',   389, 'Japan',         'Tokyo',     1, 18, 72000.00,2000.00,
 'Major automotive manufacturer. Complex multi-region deployment. Uses disaster recovery features. Has SLA of 99.99%. One open P2 incident being tracked.'),

('Anna',      'Kowalski',  'anna.k@retailchain.pl',       '+48-22-555-9000', 'ENTERPRISE','2023-01-01', '2024-12-31', 'ACTIVE',    NOW() - INTERVAL '3 days',   277, 'Poland',        'Warsaw',    0, 11, 33600.00,1200.00,
 'Retail chain IT director. 200+ stores connected. Heavy POS integration. Seasonal spikes (Christmas, Easter). Interested in AI-powered inventory forecasting.'),

-- BASIC customers (mix of engaged and at-risk)
('Kavya',     'Nair',      'kavya.nair@freelance.in',     '+91-9000011111', 'BASIC',      '2024-01-01', '2025-01-01', 'ACTIVE',    NOW() - INTERVAL '4 days',   67,  'India',         'Kochi',     0, 2,  360.00,  30.00,
 'Freelance web developer. Good engagement. Uses project tracking features. Asked about premium upgrade twice. Ready for upsell conversation.'),

('Tom',       'Baker',     'tom.baker@cafe.co.uk',        '+44-7700-900001', 'BASIC',     '2023-06-01', '2024-06-01', 'ACTIVE',    NOW() - INTERVAL '12 days', 34,  'UK',            'Manchester',0, 1,  360.00,  30.00,
 'Small cafe owner. Uses appointment booking. Occasional user. Happy but not deeply engaged.'),

('Nina',      'Petrov',    'nina.petrov@agency.ru',       '+7-495-555-1234', 'BASIC',     '2023-03-01', '2024-03-01', 'ACTIVE',    NOW() - INTERVAL '90 days',  12,  'Russia',        'Moscow',    0, 0,  270.00,  30.00,
 'Digital agency. No login in 90 days. Likely churned but subscription still active. Needs re-engagement or cancel.'),

('Lucas',     'Dupont',    'lucas.d@startup.fr',          '+33-1-4000-5678', 'BASIC',     '2024-02-01', '2025-02-01', 'ACTIVE',    NOW() - INTERVAL '2 days',   45,  'France',        'Paris',     1, 3,  120.00,  30.00,
 'Early stage startup founder. Very engaged for a BASIC user. Has a support ticket open about API rate limits -- interested in upgrading to get higher limits.'),

('Sara',      'Lindqvist', 'sara.l@nonprofit.se',         '+46-70-555-0001', 'BASIC',     '2022-09-01', '2024-09-01', 'ACTIVE',    NOW() - INTERVAL '8 days',   89,  'Sweden',        'Stockholm', 0, 5,  720.00,  30.00,
 'Non-profit organisation. Long-term loyal basic customer. Uses event management features. Price sensitive -- would not upgrade without NGO discount.'),

('Yuki',      'Tanaka',    'yuki.tanaka@school.jp',       '+81-6-5555-9000', 'BASIC',     '2023-08-01', '2024-08-01', 'ACTIVE',    NOW() - INTERVAL '1 day',    156, 'Japan',         'Osaka',     0, 2,  450.00,  30.00,
 'School administrator. Surprisingly high login count for BASIC. Uses student management. Needs features only available on PREMIUM. Strong upgrade candidate.'),

('Carlos',    'Mendez',    'carlos.m@restaurant.mx',      '+52-55-5555-0001','BASIC',     '2023-11-01', '2024-11-01', 'ACTIVE',    NOW() - INTERVAL '3 days',   41,  'Mexico',        'Mexico City',0,1, 150.00,  30.00,
 'Restaurant owner. Uses reservation system. Happy with current plan. Has referred one friend.'),

-- FREE customers (conversion targets)
('Ritu',      'Agarwal',   'ritu.agarwal@student.edu',    NULL,              'FREE',      '2024-03-01', NULL,         'ACTIVE',    NOW() - INTERVAL '1 day',    23,  'India',         'Pune',      0, 0,  0.00,    0.00,
 'Computer science student. High engagement on free plan. Hits free tier limits daily. Strong conversion candidate for BASIC or PREMIUM student discount.'),

('Ben',       'Harris',    'ben.harris@blogger.com',      NULL,              'FREE',      '2023-12-01', NULL,         'ACTIVE',    NOW() - INTERVAL '14 days', 8,   'Australia',     'Sydney',    0, 0,  0.00,    0.00,
 'Content creator. Moderate engagement. Uses free plan for personal blog. Not yet seen the value for upgrading.'),

('Fatima',    'Al-Rashid', 'fatima.ar@startup.sa',        '+966-50-111-2222','FREE',      '2024-04-01', NULL,         'ACTIVE',    NOW() - INTERVAL '2 days',   67,  'Saudi Arabia',  'Riyadh',    0, 0,  0.00,    0.00,
 'Founder of an early-stage startup. Very active on free plan. Has asked about startup discount program. High conversion potential.'),

-- INACTIVE / CHURNED customers (churn analysis examples)
('Mike',      'Thompson',  'mike.t@oldcompany.com',       '+1-650-555-0099', 'PREMIUM',   '2022-01-01', '2023-01-01', 'CHURNED',   NOW() - INTERVAL '400 days', 0,   'USA',           'Seattle',   0, 8,  4800.00, 0.00,
 'Churned after 1 year premium. Cited switching to competitor. Last ticket was a complaint about missing reporting feature. Competitor has since had outages -- re-engagement opportunity.'),

('Olga',      'Ivanova',   'olga.i@bakery.ru',            '+7-812-555-6789', 'BASIC',     '2022-06-01', '2023-06-01', 'CHURNED',   NOW() - INTERVAL '300 days', 0,   'Russia',        'St Petersburg',0,2,360.00, 0.00,
 'Small bakery. Churned after annual plan ended. No re-engagement attempts made. Could be won back with local market pricing.'),

('Peter',     'Schmidt',   'peter.s@factory.de',          '+49-89-555-1111', 'ENTERPRISE','2021-01-01', '2022-12-31', 'CHURNED',   NOW() - INTERVAL '500 days', 0,   'Germany',       'Munich',    0, 31, 57600.00,0.00,
 'Large manufacturing plant. Did not renew enterprise contract. Moved to on-premise solution for data sovereignty reasons. Potential return if we offer private cloud option.'),

('Amira',     'Hassan',    'amira.h@consulting.eg',       '+20-10-555-9876', 'BASIC',     '2023-01-01', '2023-12-31', 'INACTIVE',  NOW() - INTERVAL '180 days', 3,   'Egypt',         'Cairo',     0, 1,  330.00,  0.00,
 'Consulting firm. Account inactive for 6 months. Subscription lapsed but never formally churned. Re-engagement email sent 2 months ago -- no response.'),

('John',      'Murphy',    'john.murphy@pub.ie',          '+353-1-555-0088', 'FREE',      '2024-01-01', NULL,         'INACTIVE',  NOW() - INTERVAL '120 days', 2,   'Ireland',       'Dublin',    0, 0,  0.00,    0.00,
 'Pub owner. Signed up but barely used the product. Might need onboarding help to see value.'),

-- More ACTIVE mix
('Valentina', 'Cruz',      'v.cruz@media.co',             '+1-305-555-0055', 'PREMIUM',   '2023-06-01', '2024-06-01', 'ACTIVE',    NOW() - INTERVAL '1 day',    234, 'USA',           'Miami',     0, 4,  4800.00, 400.00,
 'Media production company. Heavy video asset management usage. Power user. Loves the API. Has built internal tools on top of our API. Likely to upgrade to enterprise.'),

('Sven',      'Eriksson',  'sven.e@engineering.se',       '+46-8-555-2222',  'PREMIUM',   '2023-04-01', '2024-04-01', 'ACTIVE',    NOW() - INTERVAL '10 days', 112, 'Sweden',        'Gothenburg',0, 6, 4800.00, 400.00,
 'Engineering consultancy. Uses document management and project collaboration. Stable account. Mid-tier engagement.'),

('Nadia',     'Okafor',    'nadia.o@fintech.ng',          '+234-80-555-1234','BASIC',     '2024-01-01', '2025-01-01', 'ACTIVE',    NOW() - INTERVAL '3 days',   78,  'Nigeria',       'Lagos',     1, 4,  150.00,  30.00,
 'Fintech startup. Growing fast. Open ticket about payment integration. Will likely need premium features within 3 months. Proactive upgrade conversation recommended.'),

('Diego',     'Fernandez', 'diego.f@gaming.es',           '+34-91-555-0123', 'PREMIUM',   '2023-08-01', '2024-08-01', 'ACTIVE',    NOW() - INTERVAL '1 day',    445, 'Spain',         'Madrid',    0, 9,  4800.00, 400.00,
 'Gaming company. One of the highest-frequency premium users. Uses real-time leaderboard and analytics features. Has stress-tested our platform with 100K concurrent users.'),

('Mei',       'Lin',       'mei.lin@fashion.cn',          '+86-21-5555-8888','PREMIUM',   '2022-12-01', '2024-12-01', 'ACTIVE',    NOW() - INTERVAL '4 days',   189, 'China',         'Shanghai',  0, 5,  9600.00, 400.00,
 'Fashion e-commerce. Uses inventory and customer analytics extensively. High lifetime value. Expanding to SEA markets -- may need multi-region features soon.'),

('Kwame',     'Asante',    'kwame.a@agritech.gh',         '+233-20-555-7890','BASIC',     '2024-02-01', '2025-02-01', 'ACTIVE',    NOW() - INTERVAL '5 days',   52,  'Ghana',         'Accra',     0, 2,  120.00,  30.00,
 'AgriTech startup. Innovative use case -- using our platform for crop yield tracking. Would make a great case study. High potential, early stage.'),

('Hannah',    'Fischer',   'hannah.f@health.de',          '+49-30-555-4567', 'PREMIUM',   '2023-10-01', '2024-10-01', 'ACTIVE',    NOW() - INTERVAL '2 days',   167, 'Germany',       'Berlin',    0, 3,  3600.00, 400.00,
 'Digital health platform. GDPR compliance critical. Uses data residency (EU) features heavily. Very positive NPS. Has introduced two other healthcare companies.'),

('Ibrahim',   'Al-Farsi',  'ibrahim.af@oil.om',           '+968-99-555-1234','ENTERPRISE','2023-03-01', '2026-03-01', 'ACTIVE',    NOW() - INTERVAL '2 days',   201, 'Oman',          'Muscat',    0, 7,  72000.00,2000.00,
 'Oil & gas enterprise. 3-year contract. Uses field operations and IoT data integration. Very stable high-value account. Annual executive review scheduled for Q3.'),

('Priscilla', 'Oliveira',  'p.oliveira@bank.br',          '+55-21-9999-5678','ENTERPRISE','2022-07-01', '2025-07-01', 'ACTIVE',    NOW() - INTERVAL '1 day',    334, 'Brazil',        'Rio de Janeiro',0,14,57600.00,1600.00,
 'Regional bank. Uses core banking integration and compliance reporting. Strict SLA. Account health: excellent. Contract renewal 14 months away -- start renewal prep soon.'),

('Alex',      'Novak',     'alex.novak@saas.cz',          '+420-222-555-100','BASIC',     '2023-07-01', '2024-07-01', 'ACTIVE',    NOW() - INTERVAL '6 days',   88,  'Czech Republic','Prague',    0, 3,  360.00,  30.00,
 'SaaS company. Developer-focused usage. Uses API and webhooks. Has starred our GitHub repo. Technical user who would appreciate premium API limits.'),

('Yemi',      'Adeyemi',   'yemi.a@logistics.ng',         '+234-70-555-8765','PREMIUM',   '2023-09-01', '2024-09-01', 'ACTIVE',    NOW() - INTERVAL '30 days', 67,  'Nigeria',       'Abuja',     1, 5,  3600.00, 400.00,
 'Logistics firm. Engagement declining -- last login 30 days ago. Has 1 open ticket unresolved for 2 weeks. At moderate churn risk. Needs proactive outreach.'),

('Selin',     'Yilmaz',    'selin.y@edu.tr',              '+90-212-555-9876','BASIC',     '2024-03-01', '2025-03-01', 'ACTIVE',    NOW() - INTERVAL '2 days',   44,  'Turkey',        'Istanbul',  0, 1,  90.00,   30.00,
 'Education technology company. New customer. Good early engagement. Uses course management features. Showing interest in bulk student upload -- feature only on PREMIUM.'),

('Rohan',     'Mehta',     'rohan.mehta@proptech.in',     '+91-9900012345',  'PREMIUM',   '2023-01-01', '2024-01-01', 'SUSPENDED', NOW() - INTERVAL '20 days', 34,  'India',         'Hyderabad', 0, 0,  2800.00, 0.00,
 'Account suspended due to payment failure. Auto-suspended 20 days ago. Tried to contact via email -- no response. Has been a good customer historically. Needs payment resolution.'),

('Lin',       'Feng',      'lin.feng@retail.cn',          '+86-10-5555-2222','PREMIUM',   '2022-10-01', '2024-10-01', 'ACTIVE',    NOW() - INTERVAL '1 day',    356, 'China',         'Beijing',   0, 8,  10000.00,400.00,
 'Retail chain. One of the longest-tenured premium customers. Very high lifetime value. Uses advanced analytics. Has requested custom reporting -- evaluate for enterprise move.'),

('Isabelle',  'Martin',    'isabelle.m@fashion.fr',       '+33-1-5555-8800', 'BASIC',     '2023-05-01', '2024-05-01', 'ACTIVE',    NOW() - INTERVAL '50 days', 19,  'France',        'Lyon',      0, 0,  300.00,  30.00,
 'Fashion boutique. Infrequent user. Low engagement over last 2 months. At mild churn risk. Has not used the product''s main features.'),

('Omar',      'Abdullah',  'omar.a@telecom.pk',           '+92-300-555-1234','ENTERPRISE','2023-06-01', '2025-06-01', 'ACTIVE',    NOW() - INTERVAL '1 day',    245, 'Pakistan',      'Karachi',   1, 9,  28800.00,1200.00,
 'Telecom company. Uses network operations features. One open medium-priority incident. Growing account -- added 50 seats last quarter.'),

('Ines',      'Alvarez',   'ines.a@travel.es',            '+34-93-555-7777', 'PREMIUM',   '2023-11-01', '2024-11-01', 'ACTIVE',    NOW() - INTERVAL '3 days',   143, 'Spain',         'Barcelona', 0, 4,  3600.00, 400.00,
 'Travel agency. Uses booking and customer management. Seasonal business -- peak June-August. Very satisfied. Interested in adding AI-powered travel recommendations.'),

('Grace',     'Osei',      'grace.osei@ngo.gh',           '+233-24-555-3456','FREE',      '2024-01-01', NULL,         'ACTIVE',    NOW() - INTERVAL '7 days',   29,  'Ghana',         'Kumasi',    0, 0,  0.00,    0.00,
 'NGO program coordinator. Uses free plan for volunteer management. Would upgrade if NGO discount was offered. Potential BASIC conversion with right pricing.'),

('Viktor',    'Kozlov',    'viktor.k@manufacturing.ru',   '+7-383-555-4321', 'BASIC',     '2022-04-01', '2024-04-01', 'ACTIVE',    NOW() - INTERVAL '25 days', 45,  'Russia',        'Novosibirsk',0,3, 720.00,  30.00,
 'Manufacturing plant. Steady but not growing usage. Uses shift scheduling features. Long-term customer -- 2 years on BASIC. Stable, low-maintenance account.'),

('Zoe',       'Williams',  'zoe.w@creative.au',           '+61-2-5555-9000', 'PREMIUM',   '2024-01-01', '2025-01-01', 'ACTIVE',    NOW() - INTERVAL '2 days',   98,  'Australia',     'Melbourne', 0, 2,  1600.00, 400.00,
 'Creative agency. Recent premium convert from BASIC. Still exploring premium features. High potential -- team is growing fast. Good onboarding touchpoint needed.');