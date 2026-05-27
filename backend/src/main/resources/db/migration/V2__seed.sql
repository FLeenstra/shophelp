-- Sample stores in and around Bolsward, Friesland (lat/lng used for the map route)
insert into store (name, address, latitude, longitude) values
    ('Albert Heijn Bolsward', 'Jongemastraat 18, Bolsward', 53.0731, 5.5219),
    ('Jumbo Bolsward',        'De Marne 1, Bolsward',       53.0625, 5.5360),
    ('Lidl Bolsward',         'Snekerstraat 35, Bolsward',  53.0660, 5.5400),
    ('Poiesz Bolsward',       'Kerkstraat 1, Bolsward',     53.0710, 5.5260),
    ('Aldi Sneek',            'Oosterdijk 1, Sneek',        53.0320, 5.6600);

insert into product (name, category, unit) values
    ('Milk',           'Dairy',     '1L'),
    ('Bread',          'Bakery',    'loaf'),
    ('Eggs',           'Dairy',     '12 pack'),
    ('Butter',         'Dairy',     '250g'),
    ('Coffee',         'Beverages', '500g'),
    ('Bananas',        'Produce',   'kg'),
    ('Chicken breast', 'Meat',      'kg'),
    ('Pasta',          'Pantry',    '500g'),
    ('Tomatoes',       'Produce',   'kg'),
    ('Cheese',         'Dairy',     'kg');

-- Generate a price for every (store, product). Each store gets a deterministic
-- per-product multiplier (0.85x..1.05x of the base price), so different stores
-- are cheapest for different items -- which makes the route span multiple stores.
insert into store_price (store_id, product_id, price)
select s.id,
       p.id,
       round((bp.base * (0.85 + ((s.id * 13 + p.id * 7) % 11) / 50.0))::numeric, 2)
from store s
         join product p on true
         join (values
                   ('Milk', 1.20),
                   ('Bread', 1.50),
                   ('Eggs', 2.40),
                   ('Butter', 2.20),
                   ('Coffee', 4.80),
                   ('Bananas', 1.70),
                   ('Chicken breast', 7.50),
                   ('Pasta', 1.10),
                   ('Tomatoes', 2.60),
                   ('Cheese', 9.20)
    ) as bp(name, base) on bp.name = p.name;
