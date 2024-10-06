CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS customer;
CREATE TABLE customer
(
    id          varchar(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        varchar(50) NULL,
    age         int4        NOT NULL    DEFAULT 0,
    create_time timestamp   NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted     bool        NOT NULL    DEFAULT false,
    "version"   int4        NOT NULL    DEFAULT 0
);

DROP TABLE IF EXISTS post;
CREATE TABLE post
(
    id          varchar(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    title       varchar(50) NOT NULL,
    author      varchar(20) NOT NULL,
    content     text,
    post_time   timestamp   NOT NULL,
    create_time timestamp   NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp   NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted     bool        NOT NULL    DEFAULT false,
    "version"   int4        NOT NULL    DEFAULT 0
);

INSERT INTO customer ("name", age)
VALUES ('Emily', 22),
       ('Stephine', 22),
       ('Lyman', 28),
       ('Randell', 25),
       ('Soraya', 26),
       ('Danielle', 27),
       ('See', 29),
       ('Britta', 29),
       ('Teresita', 29),
       ('Bruce', 27),
       ('An', 25),
       ('Donny', 22),
       ('Luke', 21),
       ('Kendall', 21),
       ('Malcom', 24),
       ('Carol', 20),
       ('Brenda', 25),
       ('Elinore', 20),
       ('Heather', 25),
       ('Ettie', 23);

INSERT INTO post (title, author, content, post_time)
VALUES ('祝贺！孙颖莎4比0胜伊藤美诚', '央视新闻',
        '刚刚，WTT中国大满贯女单1/4决赛，中国选手孙颖莎 战胜伊藤美诚，晋级四强。此前的比赛中，中国选手陈幸同、王曼昱、范思琦分别战胜各自的对手，顺利晋级半决赛。女单4强都是国乒姑娘，国乒已提前包揽冠亚军！祝贺，期待更多精彩对决！',
        '2024-10-04 19:46:00'),
       ('欧盟对华电动汽车征收反补贴税提议通过', '北京商报',
        '【#欧盟对华电动汽车征收反补贴税提议通过#】当地时间10月4日，欧盟就是否对中国电动汽车征收为期五年的反补贴税举行投票。欧盟委员会发布的声明显示，投票中欧委会对中国进口纯电动汽车征收关税的提议获得了欧盟成员国的必要支持。声明说，欧盟和中国继续努力探索替代解决方案，该解决方案必须完全符合世贸组织规定、能够充分解决委员会调查所确定的损害性补贴、并且可监控和可执行。去年10月4日，欧盟委员会启动对进口自中国的电动汽车发起反补贴调查，今年7月4日开始对中国电动汽车开征临时反补贴税。8月20日欧盟发布中国电动汽车反补贴调查终裁草案，显示拟对中国电动汽车征收17%至36.3%的反补贴税。',
        '2024-10-04 20:25:16'),
       ('国庆假期 广元朝天掀起麻柳刺绣体验热', '中国新闻网',
        '中新网四川新闻10月3日电 (刘旭)3日，走进广元市朝天区曾家镇汉王老街麻柳刺绣体验店，青砖铺成的地面古朴厚重、店铺布局设计雅致，浓厚文化气息扑面而来，琳琅满目的文创产品让人目不暇接。“这些作品真好看，我要买几幅回去送给朋友。”游客谢秀华对麻柳刺绣作品爱不释手。体验馆里，游客不仅可以购买、了解麻柳刺绣作品，还可以现场体验麻柳刺绣技艺。在国家级非物质文化遗产麻柳刺绣传承人张菊花的指导下，游客们手拿针线，体验着刺绣乐趣和独特的艺术魅力。',
        '2024-10-04 08:42:44'),
       ('九月朋友圈十大谣言出炉，你“中招”了吗？',
        '广西新闻网',
        '九月谣言内容与台风、地震、暴雨等热议话题相关，在面对此类信息时，我们应当保持理性，不轻信未经证实的信息，关注官方发布的权威消息，避免传播不实信息。共同维护安全、健康、绿色的平台生态环境。',
        '2024-10-04 08:24:11'),
       ('本轮巴以冲突已致加沙地带41802人死亡',
        '央视',
        '当地时间4日，加沙地带卫生部门发表声明称，过去24小时内，以军在加沙地带开展的军事行动共导致14人死亡、50人受伤。自去年10月7日新一轮巴以冲突爆发以来，以军在加沙地带的军事行动已导致41802名巴勒斯坦人死亡、96844人受伤。（总台记者 李享）',
        '2024-10-04 19:07:07'),
       ('日本首相石破茂发表就职演说，提及中国',
        '每日经济新闻',
        '据央视新闻，当地时间10月4日14时许，日本首相石破茂在日本国会众议院发表就任首相后的就职演说。石破茂表示，将继续推进政治改革，推进政治与金钱问题的解决，致力于恢复民众对政治的信任。他还表示，为全面加强防灾减灾对策，将新设“防灾厅”。石破茂表示，将推进与中国的战略互惠关系。',
        '2024-10-04 17:55:34');
