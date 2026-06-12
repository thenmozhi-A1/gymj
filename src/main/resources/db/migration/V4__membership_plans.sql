CREATE TABLE IF NOT EXISTS membership_plans (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  price VARCHAR(255) NOT NULL,
  duration VARCHAR(255) NOT NULL,
  badge VARCHAR(255),
  is_popular BOOLEAN DEFAULT FALSE,
  is_premium BOOLEAN DEFAULT FALSE,
  rating DOUBLE DEFAULT 4.5,
  user_count VARCHAR(255),
  image_url VARCHAR(1000),
  bonus VARCHAR(255),
  accent_color VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS membership_plan_features (
  membership_plan_id BIGINT NOT NULL,
  feature VARCHAR(500) NOT NULL,
  CONSTRAINT fk_membership_plan_features FOREIGN KEY (membership_plan_id) REFERENCES membership_plans(id) ON DELETE CASCADE
);

-- Insert initial data based on existing hardcoded plans
INSERT INTO membership_plans (id, title, price, duration, badge, is_popular, is_premium, rating, user_count, image_url, bonus, accent_color) VALUES 
(1, 'Standard Plan', '5000', 'Per Month', 'Budget Friendly', false, false, 4.5, '5k+ Members', 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=1470&auto=format&fit=crop', '7-Day Money Back Guarantee', '#3b82f6'),
(2, 'Pro Membership', '9000', 'Per 6 Months', 'Most Popular', true, false, 4.8, '2.5k+ Members', 'https://images.unsplash.com/photo-1541534741688-6078c6bfb5c5?q=80&w=1470&auto=format&fit=crop', '10% Discount on Supplements', '#f97316'),
(3, 'Elite Yearly', '12000', 'Per Year', 'Best Value', false, true, 5.0, '800+ Members', 'https://images.unsplash.com/photo-1593079831268-3381b0db4a77?q=80&w=1469&auto=format&fit=crop', 'Includes Free Gym Apparel', '#ef4444'),
(4, 'VIP Yearly', '18000', 'Per Year', 'Ultimate Experience', false, true, 5.0, '300+ Members', 'https://images.unsplash.com/photo-1540497077202-7c8a3999166f?q=80&w=1470&auto=format&fit=crop', 'VIP Event Invitations', '#ffc107'),
(5, 'Custom Plan', 'Custom', 'Flexible', 'For Teams/Groups', false, false, 4.9, '50+ Corporate Teams', 'https://images.unsplash.com/photo-1571902943202-507ec2618e8f?q=80&w=1375&auto=format&fit=crop', 'Dedicated Account Manager', '#a855f7');

-- Features for Standard Plan
INSERT INTO membership_plan_features (membership_plan_id, feature) VALUES 
(1, 'Access during Peak Hours'), (1, 'Basic Workout Routines'), (1, 'Standard Gym Equipment'), (1, 'Locker Room Access'), (1, 'Free Hydration Station'), (1, 'Online Support Community');

-- Features for Pro Membership
INSERT INTO membership_plan_features (membership_plan_id, feature) VALUES 
(2, 'Full Access (6 AM - Midnight)'), (2, '4 PT Sessions per Month'), (2, 'Standard Nutritional Guide'), (2, 'Locker & Shower Facilities'), (2, 'Access to Yoga & HIIT Classes'), (2, 'Monthly Body Scan Analysis');

-- Features for Elite Yearly
INSERT INTO membership_plan_features (membership_plan_id, feature) VALUES 
(3, '24/7 Access to All Gyms'), (3, 'Unlimited Personal Training'), (3, 'Customized Macro Plans'), (3, 'Spa & Recovery Zone'), (3, 'Free Supplement Monthly Kit'), (3, 'Biometric Health Tracking');

-- Features for VIP Yearly
INSERT INTO membership_plan_features (membership_plan_id, feature) VALUES 
(4, 'Everything in Elite Plan'), (4, 'Personal Nutritionist'), (4, 'Home Workout Equipment Hire'), (4, 'Monthly Massage Therapy'), (4, 'Guest Pass for Friends'), (4, 'Private Locker with Name');

-- Features for Custom Plan
INSERT INTO membership_plan_features (membership_plan_id, feature) VALUES 
(5, 'Tailored Group Sessions'), (5, 'Corporate Wellness Programs'), (5, 'Custom Training Modules'), (5, 'Flexible Timing Slots'), (5, 'Team Progress Reports'), (5, 'Special Event Hosting');
