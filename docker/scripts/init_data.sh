#!/bin/bash
set -e

# postgres用户和默认数据库，可从环境变量获取
POSTGRES_USER="${POSTGRES_USER:-postgres}"

# 创建数据库
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
    CREATE DATABASE customer_dev;
    CREATE DATABASE post_dev;
EOSQL

# 在 customer_dev 数据库中创建表和插入数据
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "customer_dev" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

    CREATE TABLE customer
    (
        id          varchar(36)  PRIMARY KEY DEFAULT uuid_generate_v4(),
        name        varchar(50)  NULL,
        age         int4         NOT NULL    DEFAULT 0,
        address     varchar(500) NULL,
        create_time timestamp    NOT NULL    DEFAULT CURRENT_TIMESTAMP,
        deleted     bool         NOT NULL    DEFAULT false,
        "version"   int4         NOT NULL    DEFAULT 0
    );

    INSERT INTO customer (id, "name", age, address)
    VALUES ('3356dc3b-05aa-44b5-8bed-a1d73542fb9a', 'Emily', 22, '广东省广州市天河区体育西路中华广场公寓3栋1505室'),
           ('7d33ced5-ede3-4819-ab75-d098b312e43f', 'Kendall', 21, '北京市朝阳区三里屯街道京沙小区B区6号楼702室'),
           ('15efe659-67c4-43bb-8c77-27acc809a9f9', 'Ettie', 23, '上海市浦东新区张江镇欣荣家园18号楼501室');
EOSQL

# 在 post_dev 数据库中创建表和插入数据
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "post_dev" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

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

    CREATE TABLE post_like
    (
        id          varchar(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
        post_id     varchar(36) NOT NULL REFERENCES post(id),
        customer_id varchar(36) NOT NULL,
        create_time timestamp   NOT NULL    DEFAULT CURRENT_TIMESTAMP,
        deleted     bool        NOT NULL    DEFAULT false,
        "version"   int4        NOT NULL    DEFAULT 0,
        UNIQUE (post_id, customer_id)
    );

    INSERT INTO post (id, title, author, content, post_time)
    VALUES ('6ea8c56c-157e-4ee7-88ad-af5179805e08',
            '祝贺！孙颖莎4比0胜伊藤美诚', '央视新闻',
            '刚刚，WTT中国大满贯女单1/4决赛，中国选手孙颖莎 战胜伊藤美诚，晋级四强。此前的比赛中，中国选手陈幸同、王曼昱、范思琦分别战胜各自的对手，顺利晋级半决赛。女单4强都是国乒姑娘，国乒已提前包揽冠亚军！祝贺，期待更多精彩对决！',
            '2024-10-04 19:46:00'),
           ('bd61948d-845e-4cee-b368-a01457f81a22',
            '欧盟对华电动汽车征收反补贴税提议通过', '北京商报',
            '【#欧盟对华电动汽车征收反补贴税提议通过#】当地时间10月4日，欧盟就是否对中国电动汽车征收为期五年的反补贴税举行投票。欧盟委员会发布的声明显示，投票中欧委会对中国进口纯电动汽车征收关税的提议获得了欧盟成员国的必要支持。声明说，欧盟和中国继续努力探索替代解决方案，该解决方案必须完全符合世贸组织规定、能够充分解决委员会调查所确定的损害性补贴、并且可监控和可执行。去年10月4日，欧盟委员会启动对进口自中国的电动汽车发起反补贴调查，今年7月4日开始对中国电动汽车开征临时反补贴税。8月20日欧盟发布中国电动汽车反补贴调查终裁草案，显示拟对中国电动汽车征收17%至36.3%的反补贴税。',
            '2024-10-04 20:25:16'),
           ('ebe272fa-2a2d-4ad8-8e42-3f4efe0fd22e',
            '国庆假期 广元朝天掀起麻柳刺绣体验热', '中国新闻网',
            '中新网四川新闻10月3日电 (刘旭)3日，走进广元市朝天区曾家镇汉王老街麻柳刺绣体验店，青砖铺成的地面古朴厚重、店铺布局设计雅致，浓厚文化气息扑面而来，琳琅满目的文创产品让人目不暇接。“这些作品真好看，我要买几幅回去送给朋友。”游客谢秀华对麻柳刺绣作品爱不释手。体验馆里，游客不仅可以购买、了解麻柳刺绣作品，还可以现场体验麻柳刺绣技艺。在国家级非物质文化遗产麻柳刺绣传承人张菊花的指导下，游客们手拿针线，体验着刺绣乐趣和独特的艺术魅力。',
            '2024-10-04 08:42:44'),
           ('87865c9b-d983-4ea1-9d77-df46900c81d3',
            '九月朋友圈十大谣言出炉，你“中招”了吗？',
            '广西新闻网',
            '九月谣言内容与台风、地震、暴雨等热议话题相关，在面对此类信息时，我们应当保持理性，不轻信未经证实的信息，关注官方发布的权威消息，避免传播不实信息。共同维护安全、健康、绿色的平台生态环境。',
            '2024-10-04 08:24:11'),
           ('171e5719-a396-4182-b3bf-25af8dc21f6f',
            '本轮巴以冲突已致加沙地带41802人死亡',
            '央视',
            '当地时间4日，加沙地带卫生部门发表声明称，过去24小时内，以军在加沙地带开展的军事行动共导致14人死亡、50人受伤。自去年10月7日新一轮巴以冲突爆发以来，以军在加沙地带的军事行动已导致41802名巴勒斯坦人死亡、96844人受伤。（总台记者 李享）',
            '2024-10-04 19:07:07'),
           ('09acd9ca-b571-439d-8c9c-75ac1c2403f7',
            '日本首相石破茂发表就职演说，提及中国',
            '每日经济新闻',
            '据央视新闻，当地时间10月4日14时许，日本首相石破茂在日本国会众议院发表就任首相后的就职演说。石破茂表示，将继续推进政治改革，推进政治与金钱问题的解决，致力于恢复民众对政治的信任。他还表示，为全面加强防灾减灾对策，将新设“防灾厅”。石破茂表示，将推进与中国的战略互惠关系。',
            '2024-10-04 17:55:34');

    INSERT INTO post_like (post_id, customer_id)
    VALUES ('6ea8c56c-157e-4ee7-88ad-af5179805e08',
            '3356dc3b-05aa-44b5-8bed-a1d73542fb9a'),
           ('6ea8c56c-157e-4ee7-88ad-af5179805e08',
            '7d33ced5-ede3-4819-ab75-d098b312e43f'),
           ('bd61948d-845e-4cee-b368-a01457f81a22',
            '15efe659-67c4-43bb-8c77-27acc809a9f9'),
           ('ebe272fa-2a2d-4ad8-8e42-3f4efe0fd22e',
            '3356dc3b-05aa-44b5-8bed-a1d73542fb9a');
EOSQL