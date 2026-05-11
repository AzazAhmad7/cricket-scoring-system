INSERT INTO teams(name,short_name,country,active)
VALUES
    ('India','IND','India',true),
    ('Australia','AUS','Australia',true);

INSERT INTO venues(
    name,
    city,
    state,
    country,
    pitch_type,
    straight_boundary_meters,
    square_boundary_meters,
    capacity
)
VALUES
    ('Wankhede Stadium','Mumbai','Maharashtra','India','Batting',75, 65,33000),
    ('Eden garder','Kolkata','West Bengal','India','Batting',80, 55,35000);

INSERT INTO players(
    full_name,
    short_name,
    role,
    batting_style,
    bowling_style,
    team_id,
    captain,
    wicket_keeper,
    active
)
VALUES
    ('Rohit Sharma','Rohit','BATTER','RIGHT_HAND_BAT',NULL,1,true,false,true),
    ('Shubman Gill','Gill','BATTER','RIGHT_HAND_BAT',NULL,1,false,false,true),
    ('Virat Kohli','Kohli','BATTER','RIGHT_HAND_BAT',NULL,1,false,false,true),
    ('KL Rahul','Rahul','WICKET_KEEPER','RIGHT_HAND_BAT',NULL,1,false,true,true),
    ('Hardik Pandya','Hardik','ALL_ROUNDER','RIGHT_HAND_BAT','RIGHT_ARM_FAST',1,false,false,true),
    ('Ravindra Jadeja','Jadeja','ALL_ROUNDER','LEFT_HAND_BAT','LEFT_ARM_SPIN',1,false,false,true),
    ('Kuldeep Yadav','Kuldeep','BOWLER','LEFT_HAND_BAT','LEFT_ARM_SPIN',1,false,false,true),
    ('Bumrah','Bumrah','BOWLER','RIGHT_HAND_BAT','RIGHT_ARM_FAST',1,false,false,true),
    ('Shami','Shami','BOWLER','RIGHT_HAND_BAT','RIGHT_ARM_FAST',1,false,false,true),
    ('Siraj','Siraj','BOWLER','RIGHT_HAND_BAT','RIGHT_ARM_FAST',1,false,false,true),
    ('Axar Patel','Axar','ALL_ROUNDER','LEFT_HAND_BAT','LEFT_ARM_SPIN',1,false,false,true),
    ('Ishan Kishan','Ishan','WICKET_KEEPER','LEFT_HAND_BAT',NULL,1,false,true,true),
    ('Rinku Singh','Rinku','BATTER','LEFT_HAND_BAT',NULL,1,false,false,true),
    ('Yashasvi Jaiswal','Jaiswal','BATTER','LEFT_HAND_BAT',NULL,1,false,false,true),
    ('Arshdeep Singh','Arshdeep','BOWLER','LEFT_HAND_BAT','LEFT_ARM_FAST',1,false,false,true),
    ('Ruturaj Gaikwad','Ruturaj','BATTER','RIGHT_HAND_BAT',NULL,1,false,false,true),
    ('Tilak Varma','Tilak','BATTER','LEFT_HAND_BAT',NULL,1,false,false,true),
    ('Washington Sundar','Sundar','ALL_ROUNDER','LEFT_HAND_BAT','RIGHT_ARM_OFFBREAK',1,false,false,true),
    ('Prasidh Krishna','Prasidh','BOWLER','RIGHT_HAND_BAT','RIGHT_ARM_FAST',1,false,false,true),
    ('Avesh Khan','Avesh','BOWLER','RIGHT_HAND_BAT','RIGHT_ARM_FAST',1,false,false,true);

INSERT INTO players(
    full_name,
    short_name,
    role,
    batting_style,
    bowling_style,
    team_id,
    captain,
    wicket_keeper,
    active
)
VALUES
    ('David Warner','Warner','BATTER','LEFT_HAND_BAT',NULL,2,false,false,true),
    ('Head','Head','BATTER','LEFT_HAND_BAT',NULL,2,false,false,true),
    ('Steve Smith','Smith','BATTER','RIGHT_HAND_BAT',NULL,2,false,false,true),
    ('Labuschagne','Marnus','BATTER','RIGHT_HAND_BAT',NULL,2,false,false,true),
    ('Josh Inglis','Inglis','WICKET_KEEPER','RIGHT_HAND_BAT',NULL,2,false,true,true),
    ('Maxwell','Maxwell','ALL_ROUNDER','RIGHT_HAND_BAT','RIGHT_ARM_OFFBREAK',2,false,false,true),
    ('Starc','Starc','BOWLER','LEFT_HAND_BAT','LEFT_ARM_FAST',2,false,false,true),
    ('Cummins','Cummins','BOWLER','RIGHT_HAND_BAT','RIGHT_ARM_FAST',2,true,false,true),
    ('Hazlewood','Hazlewood','BOWLER','RIGHT_HAND_BAT','RIGHT_ARM_FAST',2,false,false,true),
    ('Zampa','Zampa','BOWLER','RIGHT_HAND_BAT','RIGHT_ARM_LEGBREAK',2,false,false,true),
    ('Stoinis','Stoinis','ALL_ROUNDER','RIGHT_HAND_BAT','RIGHT_ARM_MEDIUM',2,false,false,true),
    ('Cameron Green','Green','ALL_ROUNDER','RIGHT_HAND_BAT','RIGHT_ARM_MEDIUM',2,false,false,true),
    ('Alex Carey','Carey','WICKET_KEEPER','LEFT_HAND_BAT',NULL,2,false,true,true),
    ('Nathan Ellis','Ellis','BOWLER','RIGHT_HAND_BAT','RIGHT_ARM_FAST',2,false,false,true),
    ('Marcus Harris','Harris','BATTER','LEFT_HAND_BAT',NULL,2,false,false,true),
    ('Matthew Short','Short','BATTER','RIGHT_HAND_BAT','RIGHT_ARM_OFFBREAK',2,false,false,true),
    ('Josh Philippe','Philippe','WICKET_KEEPER','RIGHT_HAND_BAT',NULL,2,false,true,true),
    ('Sean Abbott','Abbott','ALL_ROUNDER','RIGHT_HAND_BAT','RIGHT_ARM_FAST',2,false,false,true),
    ('Nathan Lyon','Lyon','BOWLER','RIGHT_HAND_BAT','RIGHT_ARM_OFFBREAK',2,false,false,true),
    ('Spencer Johnson','Johnson','BOWLER','LEFT_HAND_BAT','LEFT_ARM_FAST',2,false,false,true);