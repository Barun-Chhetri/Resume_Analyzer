-- ==========================================================
-- Flyway Migration V3: Seed Canonical Skills & Aliases
-- Comprehensive taxonomy covering Languages, Frameworks,
-- Databases, Cloud/DevOps, AI/ML, Architecture, and Testing.
-- ==========================================================

-- Helper function or direct inserts
CREATE OR REPLACE FUNCTION insert_skill_with_aliases(
    p_name VARCHAR,
    p_norm VARCHAR,
    p_cat VARCHAR,
    p_desc VARCHAR,
    p_aliases VARCHAR[]
) RETURNS VOID AS $$
DECLARE
    v_skill_id UUID;
    v_alias VARCHAR;
    v_norm_alias VARCHAR;
BEGIN
    SELECT id INTO v_skill_id FROM canonical_skills WHERE normalized_name = p_norm;
    IF v_skill_id IS NULL THEN
        v_skill_id := gen_random_uuid();
        INSERT INTO canonical_skills (id, canonical_name, normalized_name, category, description, created_at, updated_at)
        VALUES (v_skill_id, p_name, p_norm, p_cat, p_desc, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
    END IF;

    -- Add canonical name as alias
    v_norm_alias := lower(regexp_replace(p_name, '[^a-zA-Z0-9]', '', 'g'));
    INSERT INTO skill_aliases (id, canonical_skill_id, alias, normalized_alias)
    VALUES (gen_random_uuid(), v_skill_id, p_name, v_norm_alias)
    ON CONFLICT DO NOTHING;

    -- Add aliases
    IF p_aliases IS NOT NULL THEN
        FOREACH v_alias IN ARRAY p_aliases LOOP
            v_norm_alias := lower(regexp_replace(v_alias, '[^a-zA-Z0-9]', '', 'g'));
            INSERT INTO skill_aliases (id, canonical_skill_id, alias, normalized_alias)
            VALUES (gen_random_uuid(), v_skill_id, v_alias, v_norm_alias)
            ON CONFLICT DO NOTHING;
        END LOOP;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Languages
SELECT insert_skill_with_aliases('Java', 'java', 'Languages', 'Object-oriented programming language widely used in enterprise backend development.', ARRAY['java', 'core java', 'java 21', 'java 17', 'java 11', 'java 8', 'java se', 'java ee', 'j2ee']);
SELECT insert_skill_with_aliases('Python', 'python', 'Languages', 'Interpreted high-level programming language dominant in AI, ML, and scripting.', ARRAY['python', 'python 3', 'python3', 'py']);
SELECT insert_skill_with_aliases('JavaScript', 'javascript', 'Languages', 'Dynamic web programming language running in browsers and Node.js.', ARRAY['javascript', 'js', 'es6', 'es2020', 'es2022', 'vanilla js', 'modern javascript']);
SELECT insert_skill_with_aliases('TypeScript', 'typescript', 'Languages', 'Typed superset of JavaScript providing static types for scalable applications.', ARRAY['typescript', 'ts']);
SELECT insert_skill_with_aliases('SQL', 'sql', 'Languages', 'Standard query language for relational database management systems.', ARRAY['sql', 'relational database', 'rdbms', 'structured query language']);
SELECT insert_skill_with_aliases('C++', 'cpp', 'Languages', 'High-performance systems programming language with low-level memory control.', ARRAY['c++', 'cpp', 'c plus plus']);
SELECT insert_skill_with_aliases('C#', 'csharp', 'Languages', 'Modern object-oriented language developed by Microsoft for .NET ecosystem.', ARRAY['c#', 'csharp', 'c sharp', '.net c#']);
SELECT insert_skill_with_aliases('Go', 'go', 'Languages', 'Statically typed compiled language designed by Google for concurrent distributed systems.', ARRAY['go', 'golang']);
SELECT insert_skill_with_aliases('Rust', 'rust', 'Languages', 'Memory-safe systems programming language focusing on performance and safety without garbage collection.', ARRAY['rust', 'rustlang']);
SELECT insert_skill_with_aliases('HTML/CSS', 'htmlcss', 'Languages', 'Standard markup and styling technologies for building web user interfaces.', ARRAY['html', 'css', 'html5', 'css3', 'html/css']);

-- Frameworks & Libraries
SELECT insert_skill_with_aliases('Spring Boot', 'springboot', 'Frameworks', 'Enterprise Java framework for production-ready stand-alone microservices.', ARRAY['spring', 'spring boot', 'spring framework', 'springboot', 'spring mvc', 'spring data jpa', 'spring security']);
SELECT insert_skill_with_aliases('Spring AI', 'springai', 'Frameworks', 'Spring framework project providing portable AI and RAG application abstractions.', ARRAY['spring ai', 'springai', 'spring-ai']);
SELECT insert_skill_with_aliases('React', 'react', 'Frameworks', 'Declarative component-based JavaScript library for building modern user interfaces.', ARRAY['react', 'reactjs', 'react.js', 'react 18']);
SELECT insert_skill_with_aliases('Next.js', 'nextjs', 'Frameworks', 'React framework for server-rendered and static web applications.', ARRAY['next.js', 'nextjs', 'next']);
SELECT insert_skill_with_aliases('Node.js', 'nodejs', 'Frameworks', 'Asynchronous event-driven JavaScript runtime built on Chrome V8.', ARRAY['node', 'nodejs', 'node.js']);
SELECT insert_skill_with_aliases('Express.js', 'expressjs', 'Frameworks', 'Fast, unopinionated, minimalist web framework for Node.js.', ARRAY['express', 'express.js', 'expressjs']);
SELECT insert_skill_with_aliases('Angular', 'angular', 'Frameworks', 'TypeScript-based open-source web application framework led by Google.', ARRAY['angular', 'angularjs', 'angular 2+']);
SELECT insert_skill_with_aliases('Vue.js', 'vuejs', 'Frameworks', 'Progressive JavaScript framework for building user interfaces.', ARRAY['vue', 'vue.js', 'vuejs', 'vue 3']);
SELECT insert_skill_with_aliases('Django', 'django', 'Frameworks', 'High-level Python web framework that encourages rapid development and clean design.', ARRAY['django', 'django rest framework', 'drf']);
SELECT insert_skill_with_aliases('FastAPI', 'fastapi', 'Frameworks', 'Modern, fast web framework for building APIs with Python 3.8+ based on standard Python type hints.', ARRAY['fastapi', 'fast api']);

-- Databases & Storage
SELECT insert_skill_with_aliases('PostgreSQL', 'postgresql', 'Databases', 'Advanced open-source object-relational database system with rich indexing and extension support.', ARRAY['postgresql', 'postgres', 'postgres sql', 'psql']);
SELECT insert_skill_with_aliases('PGVector', 'pgvector', 'Databases', 'Open-source vector similarity search extension for PostgreSQL.', ARRAY['pgvector', 'pg-vector', 'pg vector', 'postgres vector']);
SELECT insert_skill_with_aliases('MySQL', 'mysql', 'Databases', 'Widely deployed open-source relational database management system.', ARRAY['mysql']);
SELECT insert_skill_with_aliases('MongoDB', 'mongodb', 'Databases', 'Document-based distributed NoSQL database designed for modern apps and cloud.', ARRAY['mongodb', 'mongo', 'nosql']);
SELECT insert_skill_with_aliases('Redis', 'redis', 'Databases', 'In-memory data structure store used as a distributed cache, message broker, and database.', ARRAY['redis', 'redis cache']);
SELECT insert_skill_with_aliases('Vector Databases', 'vectordatabases', 'Databases', 'Databases optimized for storing and querying high-dimensional vector embeddings.', ARRAY['vector db', 'vector databases', 'vector database', 'pinecone', 'milvus', 'chromadb', 'qdrant', 'weaviate']);
SELECT insert_skill_with_aliases('Elasticsearch', 'elasticsearch', 'Databases', 'Distributed, RESTful search and analytics engine based on Apache Lucene.', ARRAY['elasticsearch', 'elastic search', 'elk']);

-- Cloud & DevOps
SELECT insert_skill_with_aliases('Amazon Web Services', 'aws', 'Cloud & DevOps', 'Comprehensive cloud computing platform provided by Amazon.', ARRAY['aws', 'amazon web services', 'amazon aws', 'ec2', 's3', 'lambda', 'ecs', 'eks']);
SELECT insert_skill_with_aliases('Microsoft Azure', 'azure', 'Cloud & DevOps', 'Cloud computing platform operated by Microsoft for application management.', ARRAY['azure', 'microsoft azure', 'ms azure', 'azure devops']);
SELECT insert_skill_with_aliases('Google Cloud Platform', 'gcp', 'Cloud & DevOps', 'Suite of cloud computing services that runs on the same infrastructure that Google uses.', ARRAY['gcp', 'google cloud', 'google cloud platform']);
SELECT insert_skill_with_aliases('Docker', 'docker', 'Cloud & DevOps', 'Platform for developing, shipping, and running applications inside containers.', ARRAY['docker', 'docker compose', 'containerization', 'containers']);
SELECT insert_skill_with_aliases('Kubernetes', 'kubernetes', 'Cloud & DevOps', 'Open-source system for automating deployment, scaling, and management of containerized applications.', ARRAY['kubernetes', 'k8s']);
SELECT insert_skill_with_aliases('CI/CD', 'cicd', 'Cloud & DevOps', 'Continuous Integration and Continuous Delivery automation practices.', ARRAY['ci/cd', 'cicd', 'continuous integration', 'continuous delivery', 'continuous deployment', 'jenkins', 'github actions', 'gitlab ci']);
SELECT insert_skill_with_aliases('Terraform', 'terraform', 'Cloud & DevOps', 'Open-source infrastructure as code software tool created by HashiCorp.', ARRAY['terraform', 'iac', 'infrastructure as code']);
SELECT insert_skill_with_aliases('Linux', 'linux', 'Cloud & DevOps', 'Open-source Unix-like operating system kernel widely used in production server environments.', ARRAY['linux', 'ubuntu', 'centos', 'redhat', 'bash', 'shell scripting']);
SELECT insert_skill_with_aliases('Git', 'git', 'Cloud & DevOps', 'Distributed version control system for tracking changes in source code.', ARRAY['git', 'github', 'gitlab', 'bitbucket', 'version control']);

-- AI / ML & RAG
SELECT insert_skill_with_aliases('Retrieval-Augmented Generation', 'rag', 'AI/ML', 'AI architecture combining vector retrieval with large language models to produce grounded answers.', ARRAY['rag', 'retrieval-augmented generation', 'retrieval augmented generation', 'grounded generation']);
SELECT insert_skill_with_aliases('Machine Learning', 'machinelearning', 'AI/ML', 'Study of computer algorithms that improve automatically through experience and data.', ARRAY['machine learning', 'ml', 'scikit-learn']);
SELECT insert_skill_with_aliases('Artificial Intelligence', 'artificialintelligence', 'AI/ML', 'Simulation of human intelligence processes by computer systems.', ARRAY['artificial intelligence', 'ai', 'genai', 'generative ai']);
SELECT insert_skill_with_aliases('Natural Language Processing', 'nlp', 'AI/ML', 'Subfield of linguistics and AI focused on computer understanding of human language.', ARRAY['nlp', 'natural language processing', 'text extraction', 'tokenization']);
SELECT insert_skill_with_aliases('Embeddings', 'embeddings', 'AI/ML', 'Dense numerical vector representations of text capturing semantic relationships.', ARRAY['embeddings', 'vector embeddings', 'text embeddings', 'dense vectors']);
SELECT insert_skill_with_aliases('Large Language Models', 'llm', 'AI/ML', 'Advanced deep learning models trained on vast text data capable of language tasks.', ARRAY['llm', 'large language models', 'gpt', 'gpt-4', 'openai', 'claude', 'gemini']);

-- Architecture, Testing & Methodologies
SELECT insert_skill_with_aliases('REST API', 'restapi', 'Architecture', 'Architectural style for designing networked applications over HTTP.', ARRAY['rest', 'restful', 'rest api', 'rest apis', 'restful api', 'restful web services']);
SELECT insert_skill_with_aliases('Microservices', 'microservices', 'Architecture', 'Architectural pattern where an application is arranged as a collection of loosely coupled services.', ARRAY['microservices', 'microservice', 'micro-services', 'microservice architecture']);
SELECT insert_skill_with_aliases('GraphQL', 'graphql', 'Architecture', 'Query language for APIs and runtime for fulfilling queries with existing data.', ARRAY['graphql', 'gql']);
SELECT insert_skill_with_aliases('Apache Kafka', 'kafka', 'Architecture', 'Distributed event streaming platform for high-performance data pipelines and streaming analytics.', ARRAY['kafka', 'apache kafka', 'event streaming', 'message broker']);
SELECT insert_skill_with_aliases('System Design', 'systemdesign', 'Architecture', 'Process of defining the architecture, modules, interfaces, and data for a system to satisfy requirements.', ARRAY['system design', 'distributed systems', 'scalability', 'high availability']);
SELECT insert_skill_with_aliases('Object-Oriented Programming', 'oop', 'Architecture', 'Programming paradigm based on the concept of objects containing data and code.', ARRAY['oop', 'object-oriented programming', 'object oriented programming', 'ood', 'design patterns']);
SELECT insert_skill_with_aliases('Unit Testing', 'unittesting', 'Testing', 'Software testing method by which individual units of source code are tested for correctness.', ARRAY['unit testing', 'unit test', 'junit', 'junit 5', 'junit5', 'test driven development', 'tdd']);
SELECT insert_skill_with_aliases('Mockito', 'mockito', 'Testing', 'Open-source testing framework for Java that allows mocking of objects.', ARRAY['mockito', 'mocking', 'test mocking']);
SELECT insert_skill_with_aliases('Agile/Scrum', 'agilescrum', 'Methodologies', 'Iterative software development framework emphasizing collaboration and fast delivery.', ARRAY['agile', 'scrum', 'sprint planning', 'jira', 'kanban']);

-- Clean up helper function
DROP FUNCTION insert_skill_with_aliases;
