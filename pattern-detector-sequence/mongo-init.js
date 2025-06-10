db.createUser(
    {
        user: "dev",
        pwd: "dev",
        roles: [
            {
                role: "readWrite",
                db: "pattern-detector-sequence"
            }
        ]
    }
);