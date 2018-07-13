#!/usr/bin/env python


# [START imports]
from google.appengine.ext import ndb
import webapp2
import json
import datetime


class Book(ndb.Model):
    id = ndb.StringProperty()   # primary key
    title = ndb.StringProperty(required=True)
    author_first_name = ndb.StringProperty(required=True)
    author_last_name = ndb.StringProperty(required=True)
    page_count = ndb.IntegerProperty(required=True)
    checked_out = ndb.BooleanProperty()


class Member(ndb.Model):
    id = ndb.StringProperty()   # primary key
    first_name = ndb.StringProperty(required=True)
    last_name = ndb.StringProperty(required=True)
    phone_number = ndb.StringProperty()
    current_books = ndb.StringProperty(repeated=True)


class BooksHandler(webapp2.RequestHandler):
    """ Handles getting all books/creating a new book """
    # Add a new book
    def post(self):
        book_data = json.loads(self.request.body)
        # make sure all data fields are supplied
        if 'title' not in book_data or 'author_first_name' not in book_data or 'author_last_name' not in book_data \
                or 'page_count' not in book_data:
            self.response.set_status(404)
            self.response.write("Error: Missing Data. Refer to documentation")

        # make sure data types are correct
        elif type(book_data['title']) is not unicode or type(book_data['author_first_name']) is not unicode or \
                type(book_data['author_last_name']) is not unicode or type(book_data['page_count']) is not int \
                or book_data['page_count'] <= 0:
            self.response.set_status(404)
            self.response.write("Error: Incorrect Data Types. Refer to documentation")
        else:
            # Make sure book name is unique
            query = Book.query(Book.title.IN([book_data['title']]))
            results = list(query.fetch())
            if len(results) > 0:
                self.response.set_status(404)
                self.response.write("Error: Book name already exists")
            else:
                # add book to datastore
                new_book = Book(title=book_data['title'], author_first_name=book_data['author_first_name'],
                                author_last_name=book_data['author_last_name'], page_count=book_data['page_count'],
                                checked_out=False)
                book_key = new_book.put()

                # use datastore id for book id
                book_id = book_key.id()
                new_book.id = str(book_id)
                new_book.put()

                # return the new book data
                book_dict = new_book.to_dict()
                book_dict['self'] = '/book/' + new_book.key.urlsafe()
                self.response.headers["Content-Type"] = "application/json"
                self.response.set_status(201)
                self.response.write(json.dumps(book_dict))

    # View list of all books
    def get(self):
        books = Book.query()
        book_list = []
        for book in books:
            book_url = book.key.urlsafe()
            book = book.to_dict()
            book['self'] = '/book/' + book_url
            book_list.append(book)
        self.response.headers["Content-Type"] = "application/json"
        self.response.set_status(200)
        self.response.write(json.dumps(book_list))


class BookHandler(webapp2.RequestHandler):
    """ Handles getting/modifying/deleting a single book """
    # get info on a single book
    def get(self, id=None):
        if id:
            book = ndb.Key(urlsafe=id).get()
            if book is None:
                self.response.set_status(404)
                self.response.write("Error: Book not found")
            else:
                book_url = book.key.urlsafe()
                book = book.to_dict()
                book['self'] = '/book/' + book_url
                self.response.headers["Content-Type"] = "application/json"
                self.response.set_status(200)
                self.response.write(json.dumps(book))

    # modify a single book
    def patch(self, id=None):
        if id:
            book = ndb.Key(urlsafe=id).get()
            if book is None:
                self.response.set_status(404)
                self.response.write("Error: Book not found")
            else:
                book_data = json.loads(self.request.body)
                # update name if valid name was provided
                if 'title' in book_data and type(book_data['title']) is unicode:
                    # Make sure book name is unique
                    query = Book.query(Book.title.IN([book_data['title']]))
                    results = list(query.fetch())
                    if len(results) is 0:
                        book.title = book_data['title']

                # update author's first name of book if valid type was provided
                if 'author_first_name' in book_data and type(book_data['author_first_name']) is unicode:
                    book.author_first_name = book_data['author_first_name']

                # update author's first name of book if valid type was provided
                if 'author_last_name' in book_data and type(book_data['author_last_name']) is unicode:
                    book.author_last_name = book_data['author_last_name']

                # update book page count if valid count was provided
                if 'page_count' in book_data and type(book_data['page_count']) is int and book_data['page_count'] > 0:
                    book.page_count = book_data['page_count']
                book.put()
                # return the new book data
                book_dict = book.to_dict()
                book_dict['self'] = '/book/' + book.key.urlsafe()
                self.response.headers["Content-Type"] = "application/json"
                self.response.set_status(200)
                self.response.write(json.dumps(book_dict))

    # delete a single book
    def delete(self, id=None):
        if id:
            book = ndb.Key(urlsafe=id).get()
            if book is None:
                self.response.set_status(404)
                self.response.write("Error: Book not found")
            else:
                if book.checked_out is True:
                    # get member who has book
                    query = Member.query(Member.current_books == book.title)
                    member = query.get()
                    # remove book from current book list
                    book_index = member.current_books.index(book.title)
                    del member.current_books[book_index]
                    member.put()

                # remove the book
                book.key.delete()
                self.response.set_status(200)
                self.response.write("Book has been removed")


class MemberHandler(webapp2.RequestHandler):
    """ Handles getting all members/adding a new member """
    # get info on a single member
    def get(self, id=None):
        if id:
            member = ndb.Key(urlsafe=id).get()
            if member is None:
                self.response.set_status(404)
                self.response.write("Error: Member not found")
            else:
                member_url = member.key.urlsafe()
                member = member.to_dict()
                member['self'] = '/members/' + member_url
                self.response.headers["Content-Type"] = "application/json"
                self.response.set_status(200)
                self.response.write(json.dumps(member))

    # modify a single member
    def patch(self, id=None):
        if id:
            member = ndb.Key(urlsafe=id).get()
            if member is None:
                self.response.set_status(404)
                self.response.write("Error: Member not found")
            else:
                member_data = json.loads(self.request.body)

                # update members's first name if valid type was provided
                if 'first_name' in member_data and type(member_data['first_name']) is unicode:
                    member.first_name = member_data['first_name']

                # update members's last name if valid type was provided
                if 'last_name' in member_data and type(member_data['last_name']) is unicode:
                    member.last_name = member_data['last_name']

                # update phone number if valid data was provided
                if 'phone_number' in member_data and type(member_data['phone_number']) is unicode:
                    member.phone_number = member_data['phone_number']
                member.put()

                # return the updated member data
                member_dict = member.to_dict()
                member_dict['self'] = '/members/' + member.key.urlsafe()
                self.response.headers["Content-Type"] = "application/json"
                self.response.set_status(200)
                self.response.write(json.dumps(member_dict))

    # delete a single member
    def delete(self, id=None):
        if id:
            member = ndb.Key(urlsafe=id).get()
            if member is None:
                self.response.set_status(404)
                self.response.write("Error: Member not found")
            else:
                for c_book in member.current_books:
                    # get each book the member currently has
                    query = Book.query(Book.title == c_book)
                    book = query.get()
                    # return book
                    if book is not None:
                        book.checked_out = False
                        book.put()

                # remove the member
                member.key.delete()
                self.response.set_status(200)
                self.response.write("Member has been removed")


class MembersHandler(webapp2.RequestHandler):
    """ Handles getting/modifying/deleting a single member """
# create a new member
    def post(self):
        member_data = json.loads(self.request.body)
        # make sure all data fields are supplied
        if 'first_name' not in member_data or 'last_name' not in member_data or 'phone_number' not in member_data:
            self.response.set_status(404)
            self.response.write("Error: Missing Data. Refer to documentation")

        # make sure data types are correct
        elif type(member_data['first_name']) is not unicode or type(member_data['last_name']) is not unicode or \
                type(member_data['phone_number']) is not unicode:
            self.response.set_status(404)
            self.response.write("Error: Incorrect Data Types. Refer to documentation")
        else:
            # create the member
            new_member = Member(first_name=member_data['first_name'], last_name=member_data['last_name'],
                                phone_number=member_data['phone_number'])
            member_key = new_member.put()

            # use datastore id for member id
            member_id = member_key.id()
            new_member.id = str(member_id)
            new_member.put()

            # return the new member data
            member_dict = new_member.to_dict()
            member_dict['self'] = '/members/' + new_member.key.urlsafe()
            self.response.headers["Content-Type"] = "application/json"
            self.response.set_status(201)
            self.response.write(json.dumps(member_dict))

    # View list of all members
    def get(self):
        members = Member.query()
        member_list = []
        for member in members:
            member_url = member.key.urlsafe()
            curr_member = member.to_dict()
            curr_member['self'] = '/members/' + member_url
            member_list.append(curr_member)
        self.response.headers["Content-Type"] = "application/json"
        self.response.set_status(200)
        self.response.write(json.dumps(member_list))


class AvailableHandler(webapp2.RequestHandler):
    """ Returns all available books"""
    def get(self):
        books = Book.query()
        book_list = []
        for book in books:
            # only add the books that are available
            if book.checked_out is False:
                book_url = book.key.urlsafe()
                book = book.to_dict()
                book['self'] = '/book/' + book_url
                book_list.append(book)
        self.response.headers["Content-Type"] = "application/json"
        self.response.set_status(200)
        self.response.write(json.dumps(book_list))


class MemberBooksHandler(webapp2.RequestHandler):
    """ Returns the members current books"""
    def get(self, id=None):
        if id:
            member = ndb.Key(urlsafe=id).get()
            if member is None:
                self.response.set_status(404)
                self.response.write("Error: Member not found")
            else:
                book_list = []
                for books in member.current_books:
                    query = Book.query(Book.title == books)
                    book = query.get()
                    url = book.key.urlsafe()
                    curr_book = book.to_dict()
                    curr_book['self'] = '/book/' + url
                    book_list.append(curr_book)
                self.response.headers["Content-Type"] = "application/json"
                self.response.set_status(200)
                self.response.write(json.dumps(book_list))


class CheckOutHandler(webapp2.RequestHandler):
    """ Handles a member checking out a single book"""
    def patch(self, id=None):
        if id:
            book = ndb.Key(urlsafe=id).get()
            if book is None:
                self.response.set_status(404)
                self.response.write("Error: Book not found")
            # make sure book isn't already checked out
            elif book.checked_out:
                self.response.set_status(404)
                self.response.write("Error: Book already checked out")
            else:
                member_data = json.loads(self.request.body)

                # make sure an id was supplied
                if 'id' not in member_data:
                    self.response.set_status(404)
                    self.response.write("Error: Member ID not supplied. Refer to documentation")
                else:
                    query = Member.query(Member.id == member_data['id'])
                    member = query.get()
                    # if make sure ID was for a real member
                    if member is None:
                        self.response.set_status(404)
                        self.response.write("Error: Member not found")
                    else:
                        # change bool in book and add book id to members list of checked out books
                        book.checked_out = True
                        book.put()
                        member.current_books.append(book.title)
                        member.put()
                        self.response.set_status(200)
                        self.response.write("%s has been checked out to %s %s" %
                                            (book.title, member.first_name, member.last_name))


class ReturnHandler(webapp2.RequestHandler):
    """ Handles a member returning a single book"""
    def patch(self, id=None):
        if id:
            book = ndb.Key(urlsafe=id).get()
            if book is None:
                self.response.set_status(404)
                self.response.write("Error: Book not found")
            # make sure book is checked out
            elif book.checked_out is not True:
                self.response.set_status(404)
                self.response.write("Error: Book is not checked out")
            else:
                # get member who has book
                query = Member.query(Member.current_books == book.title)
                member = query.get()

                if member is None:
                    self.response.set_status(404)
                    self.response.write("Error: No member currently has book")
                else:
                    # remove book from current book list
                    book_index = member.current_books.index(book.title)
                    del member.current_books[book_index]
                    member.put()

                    # change bool in book
                    book.checked_out = False
                    book.put()

                    self.response.set_status(200)
                    self.response.write("%s %s has returned %s" %
                                        (member.first_name, member.last_name, book.title))


class MainPage(webapp2.RequestHandler):
    def get(self):
        self.response.write("Home")
# [END main_page]


allowed_methods = webapp2.WSGIApplication.allowed_methods
new_allowed_methods = allowed_methods.union(('PATCH',))
webapp2.WSGIApplication.allowed_methods = new_allowed_methods

# [START app]
app = webapp2.WSGIApplication([
    ('/', MainPage),
    ('/books', BooksHandler),
    ('/books/available', AvailableHandler),
    ('/book/([A-z0-9\-]+)', BookHandler),
    ('/members', MembersHandler),
    ('/members/([A-z0-9\-]+)', MemberHandler),
    ('/members/([A-z0-9\-]+)/books', MemberBooksHandler),
    ('/book/([A-z0-9\-]+)/checkout', CheckOutHandler),
    ('/book/([A-z0-9\-]+)/return', ReturnHandler),
], debug=True)
# [END app]
