db.createUser(
    {
        user: "damncan",
        pwd: "damncan",
        roles: [
            {
                role: "readWrite",
                db: "trading"
            }
        ]
    }
)